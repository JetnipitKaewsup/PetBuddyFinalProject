package com.example.petbuddy.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petbuddy.MainActivity
import com.example.petbuddy.R
import com.example.petbuddy.activity.BaseActivity
import com.example.petbuddy.adapter.EventAdapter
import com.example.petbuddy.databinding.FragmentScheduleBinding
import com.example.petbuddy.model.Event
import com.example.petbuddy.navigation.MainNavigator
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseActivity: BaseActivity
    private lateinit var navigator: MainNavigator

    // เก็บวันที่เลือก
    private var selectedDate = java.time.LocalDate.now()
    private var currentMonth = YearMonth.now()
    private var calendarHeaderFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    private var events: List<Event> = listOf()
    private lateinit var eventAdapter: EventAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity
        navigator = (requireActivity() as MainActivity).navigator
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        setupToolbar()
        setupMonthNavigation()
        setupRecyclerView()
        loadEvents()
        binding.fabAddEvent.setOnClickListener {
            navigator.navigateToAddEvent(selectedDate)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupMonthNavigation() {
        // อัพเดทชื่อเดือน
        updateMonthText()

        // ปุ่มก่อนหน้า
        binding.btnPreviousMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            binding.calendarView.scrollToMonth(currentMonth)
        }

        // ปุ่มถัดไป
        binding.btnNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            binding.calendarView.scrollToMonth(currentMonth)
        }

        // ปุ่มวันนี้
        binding.btnToday.setOnClickListener {
            currentMonth = YearMonth.now()
            binding.calendarView.scrollToMonth(currentMonth)
            selectDate(java.time.LocalDate.now())
        }

        // ฟังการเลื่อนของ CalendarView
        binding.calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            updateMonthText()
        }
    }

    private fun updateMonthText() {
        binding.tvCurrentMonth.text = currentMonth.atDay(1).format(calendarHeaderFormatter)
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val daysOfWeek = daysOfWeek(firstDayOfWeekFromLocale())

        binding.calendarView.apply {
            setup(startMonth, endMonth, firstDayOfWeek)
            scrollToMonth(currentMonth)

            // ตั้งค่า DayBinder สำหรับแสดงแต่ละวัน
            dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)

                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.bind(data)
                }
            }

            // ตั้งค่า MonthHeaderBinder สำหรับแสดงชื่อวัน
            monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)

                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // ใช้ tag เพื่อผูกข้อมูลครั้งเดียว
                    if (container.titlesContainer.tag == null) {
                        container.titlesContainer.tag = data.yearMonth
                        container.titlesContainer.children
                            .map { it as TextView }
                            .forEachIndexed { index, textView ->
                                val dayOfWeek = daysOfWeek[index]
                                val title =
                                    dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                                textView.text = title
                            }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        // สร้าง EventAdapter สำหรับแสดงรายการ events
        eventAdapter = EventAdapter { event ->

            navigator.navigateToEventDetail(event)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = eventAdapter
    }

    private fun loadEvents() {
        val userId = baseActivity.getCurrentUserIdSafe() ?: return

        baseActivity.db.collection("users")
            .document(userId)
            .collection("events")
            .get()
            .addOnSuccessListener { snapshot ->
                events = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(eventId = doc.id)
                }
                // รีเฟรชปฏิทินเพื่อแสดงจุด
                binding.calendarView.notifyCalendarChanged()
                // โหลด events ของวันที่เลือก
                loadEventsForSelectedDate()
            }
    }

    private fun loadEventsForSelectedDate() {
        val eventsForDay = events.filter { event ->
            isSameDay(event.startDate.toDate(), selectedDate)
        }
        // อัพเดท RecyclerView
        eventAdapter.submitList(eventsForDay)

        binding.tvNoEvents.visibility = if (eventsForDay.isEmpty()) View.VISIBLE else View.GONE
        binding.rvEvents.visibility = if (eventsForDay.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun isSameDay(date1: Date, date2: java.time.LocalDate): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        return cal1.get(Calendar.YEAR) == date2.year &&
                cal1.get(Calendar.DAY_OF_YEAR) == date2.dayOfYear
    }

    // ViewContainer สำหรับแต่ละวัน
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView = view.findViewById<TextView>(R.id.calendarDayText)
        val eventDot = view.findViewById<View>(R.id.eventDot)
        private var currentDay: CalendarDay? = null

        init {
            view.setOnClickListener {
                currentDay?.let { day ->
                    if (day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                    }
                }
            }
        }

        fun bind(day: CalendarDay) {
            currentDay = day
            textView.text = day.date.dayOfMonth.toString()

            // จัดการลักษณะของวันตามตำแหน่ง
            when (day.position) {
                DayPosition.MonthDate -> {
                    textView.alpha = 1f
                    textView.isEnabled = true
                }

                else -> {
                    textView.alpha = 0.3f
                    textView.isEnabled = false
                }
            }

            // ไฮไลท์วันที่ถูกเลือก
            if (day.position == DayPosition.MonthDate && day.date == selectedDate) {
                textView.setBackgroundResource(R.drawable.brown_oval_bg)
                textView.setTextColor(android.graphics.Color.WHITE)
            } else {
                textView.background = null
                textView.setTextColor(android.graphics.Color.BLACK)
            }
            val hasEvent = events.any {
                isSameDay(it.startDate.toDate(), day.date)
            }
            eventDot.visibility = if (hasEvent) View.VISIBLE else View.GONE
        }
    }

    // ViewContainer สำหรับ Header ของเดือน
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }

    private fun selectDate(date: java.time.LocalDate) {
        val oldDate = selectedDate
        selectedDate = date

        // รีเฟรชวันที่เก่าและใหม่
        binding.calendarView.notifyDateChanged(oldDate)
        binding.calendarView.notifyDateChanged(selectedDate)

        // TODO: โหลด events ของวันที่เลือก
        loadEventsForSelectedDate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}