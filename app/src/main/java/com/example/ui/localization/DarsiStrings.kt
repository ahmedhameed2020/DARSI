package com.example.ui.localization

data class DarsiStrings(
    val today: String,
    val calendar: String,
    val students: String,
    val payments: String,
    val more: String,
    val nextLesson: String,
    val todayLessons: String,
    val scheduled: String,
    val summary: String,
    val lessonsToday: String,
    val receivedToday: String,
    val amountDue: String,
    val noLessonsToday: String,
    val enjoyFreeTime: String,
    val bookLesson: String,
    val directions: String,
    val join: String,
    val open: String,
    val onlineLesson: String,
    val studentHome: String,
    val estimatedTravel: String,
    val quickAdd: String,
    val whatsapp: String
)

val DarsiEnglish = DarsiStrings(
    today = "Today",
    calendar = "Calendar",
    students = "Students",
    payments = "Payments",
    more = "More",
    nextLesson = "NEXT LESSON",
    todayLessons = "TODAY'S LESSONS",
    scheduled = "scheduled",
    summary = "SUMMARY",
    lessonsToday = "Lessons today",
    receivedToday = "Received today",
    amountDue = "Amount due",
    noLessonsToday = "No lessons today.",
    enjoyFreeTime = "Enjoy the free time — or schedule a lesson.",
    bookLesson = "Book Lesson",
    directions = "Directions",
    join = "Join",
    open = "Open",
    onlineLesson = "Online Lesson",
    studentHome = "Student Home",
    estimatedTravel = "Est. travel",
    quickAdd = "Quick Add",
    whatsapp = "WhatsApp"
)

val DarsiArabic = DarsiStrings(
    today = "اليوم",
    calendar = "التقويم",
    students = "الطلاب",
    payments = "المدفوعات",
    more = "المزيد",
    nextLesson = "الحصة القادمة",
    todayLessons = "حصص اليوم",
    scheduled = "مجدولة",
    summary = "ملخص اليوم",
    lessonsToday = "حصص اليوم",
    receivedToday = "تم التحصيل",
    amountDue = "المستحق",
    noLessonsToday = "لا توجد حصص اليوم.",
    enjoyFreeTime = "استمتع بوقتك — أو أضف حصة جديدة.",
    bookLesson = "إضافة حصة",
    directions = "الاتجاهات",
    join = "دخول",
    open = "فتح",
    onlineLesson = "حصة أونلاين",
    studentHome = "منزل الطالب",
    estimatedTravel = "مدة الوصول",
    quickAdd = "إضافة سريعة",
    whatsapp = "واتساب"
)

fun darsiStrings(language: String): DarsiStrings =
    if (language.lowercase() == "ar") DarsiArabic else DarsiEnglish
