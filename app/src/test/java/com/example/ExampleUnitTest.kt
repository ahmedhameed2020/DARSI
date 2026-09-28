package com.example

import com.example.domain.finance.FinanceCalculator
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import com.example.util.WhatsAppHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFinanceCalculatorMinorUnits() {
    val amount = 400.00
    val minor = FinanceCalculator.toMinorUnits(amount)
    assertEquals(40000L, minor)
    assertEquals(400.0, FinanceCalculator.fromMinorUnits(minor), 0.0001)

    assertEquals("QAR 400", FinanceCalculator.formatMoney(40000L, "QAR"))
    assertEquals("QAR 250.50", FinanceCalculator.formatMoney(25050L, "QAR"))
  }

  @Test
  fun testFinanceCalculatorPlans() {
    // 1. Per Lesson: 100 QAR * 3 lessons = 300 QAR (30000 minor)
    val perLessonExpected = FinanceCalculator.calculateExpectedAmount(
      paymentType = "PER_LESSON",
      defaultPriceMinor = 10000L,
      completedBillableLessons = 3
    )
    assertEquals(30000L, perLessonExpected)

    // 2. Monthly: 500 QAR/mo for 2 months = 1000 QAR
    val monthlyExpected = FinanceCalculator.calculateExpectedAmount(
      paymentType = "MONTHLY",
      defaultPriceMinor = 50000L,
      completedBillableLessons = 7,
      monthsActive = 2
    )
    assertEquals(100000L, monthlyExpected)

    // 3. Package: 800 QAR package
    val packageExpected = FinanceCalculator.calculateExpectedAmount(
      paymentType = "PACKAGE",
      defaultPriceMinor = 10000L,
      completedBillableLessons = 5,
      packagePriceMinor = 80000L
    )
    assertEquals(80000L, packageExpected)

    // 4. Custom: Full Term 1800 QAR
    val customExpected = FinanceCalculator.calculateExpectedAmount(
      paymentType = "CUSTOM",
      defaultPriceMinor = 0L,
      completedBillableLessons = 10,
      customAmountMinor = 180000L
    )
    assertEquals(180000L, customExpected)
  }

  @Test
  fun testFinanceCalculatorBalances() {
    // 1000 QAR expected, 600 QAR paid => 400 QAR due
    val due = FinanceCalculator.calculateStudentBalance(100000L, 60000L)
    assertEquals(40000L, due)

    // Overpaid or settled: 1000 QAR expected, 1200 QAR paid => 0 due
    val overpaidDue = FinanceCalculator.calculateStudentBalance(100000L, 120000L)
    assertEquals(0L, overpaidDue)

    // Monthly due: 500 QAR fee, 200 paid this month => 300 QAR due
    val monthlyDue = FinanceCalculator.calculateMonthlyDue(50000L, 20000L)
    assertEquals(30000L, monthlyDue)

    // Package remaining: 8 lessons, 5 used => 3 remaining
    val remaining = FinanceCalculator.calculatePackageRemaining(8, 5)
    assertEquals(3, remaining)

    // Sum payments
    val payments = listOf(20000L, 30000L, 15000L)
    val totalReceived = FinanceCalculator.calculatePaymentsReceived(payments)
    assertEquals(65000L, totalReceived)
  }

  @Test
  fun testCurrencyFormatting() {
    assertEquals("QAR 300", CurrencyUtils.format(300.0, "QAR"))
    assertEquals("SAR 250.50", CurrencyUtils.format(250.50, "SAR"))
  }

  @Test
  fun testWhatsAppMessageFormatting() {
    val reminder = WhatsAppHelper.createLessonReminderMessage("Ahmed", "English", "4:30 PM", "Student Home")
    assertTrue(reminder.contains("Ahmed"))
    assertTrue(reminder.contains("English"))
    assertTrue(reminder.contains("4:30 PM"))

    val paymentMsg = WhatsAppHelper.createPaymentReminderMessage("Khalid", 300.0, "QAR")
    assertTrue(paymentMsg.contains("Khalid"))
    assertTrue(paymentMsg.contains("QAR 300"))
  }

  @Test
  fun testCountdownFormatting() {
    val now = System.currentTimeMillis()
    val futureIn42Min = now + (42 * 60 * 1000L)
    val end = futureIn42Min + (60 * 60 * 1000L)
    val countdown = DateTimeUtils.getCountdownString(futureIn42Min, end)
    assertTrue(countdown.contains("42 min") || countdown.contains("41 min"))
  }

  @Test
  fun testScheduleConflictDetectionLogic() {
    val startA = 1000L * 60 * 60 * 16 // 16:00
    val endA = 1000L * 60 * 60 * 17 // 17:00

    // Overlapping lesson: 16:30 - 17:30
    val startB = 1000L * 60 * 60 * 16 + (30 * 60 * 1000L)
    val endB = startB + (60 * 60 * 1000L)

    val hasConflict = (startA < endB && endA > startB)
    assertTrue("Should detect overlap", hasConflict)

    // Non-overlapping lesson: 17:00 - 18:00
    val startC = endA
    val endC = startC + (60 * 60 * 1000L)
    val hasConflictC = (startA < endC && endA > startC)
    assertFalse("Consecutive lesson should not conflict", hasConflictC)
  }

  @Test
  fun testPackageBalanceTracking() {
    val totalLessons = 8
    var usedLessons = 5
    val remaining = totalLessons - usedLessons
    assertEquals(3, remaining)

    // After 1 lesson completed
    usedLessons += 1
    assertEquals(2, totalLessons - usedLessons)
    assertEquals(0.75f, usedLessons.toFloat() / totalLessons, 0.001f)
  }

  @Test
  fun testPaymentDueCalculation() {
    val defaultPrice = 250.0
    val completedLessons = 3
    val totalBilled = defaultPrice * completedLessons // 750
    val totalPaid = 500.0
    val balanceDue = maxOf(0.0, totalBilled - totalPaid)
    assertEquals(250.0, balanceDue, 0.001)
  }

  @Test
  fun testDepartureSuggestionCalculation() {
    val cal = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 17)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val lessonStart = cal.timeInMillis
    val travelTimeMinutes = 25
    val bufferMinutes = 10

    // Departure should be 17:00 - 35 min = 16:25 (4:25 PM)
    val suggestion = com.example.util.MapsAndLocationHelper.formatDepartureSuggestion(
      startEpochMillis = lessonStart,
      travelTimeMinutes = travelTimeMinutes,
      bufferMinutes = bufferMinutes
    )
    assertNotNull(suggestion)
    assertTrue("Should suggest 4:25 PM departure: $suggestion", suggestion!!.contains("4:25"))
  }

  @Test
  fun testTravelTimeConflictLogic() {
    // Lesson A ends at 4:30 PM (16:30)
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 16)
    cal.set(java.util.Calendar.MINUTE, 30)
    val lessonAEnd = cal.timeInMillis

    // Next lesson starts at 5:00 PM (17:00)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 17)
    cal.set(java.util.Calendar.MINUTE, 0)
    val nextLessonStart = cal.timeInMillis

    val availableMinutes = (nextLessonStart - lessonAEnd) / (60 * 1000L) // 30 minutes
    val estimatedTravelTime = 40 // 40 minutes needed

    val isConflict = availableMinutes < estimatedTravelTime
    assertTrue("Should detect insufficient travel time between lessons", isConflict)
    assertEquals(30L, availableMinutes)
  }

  @Test
  fun testDarsiSimpleV1OnboardingDefaults() {
    val settings = com.example.data.local.entity.TutorSettingsEntity(
      tutorName = "Ahmed",
      defaultDurationMinutes = 60,
      defaultCurrency = "QAR",
      appLanguage = "ar",
      isOnboardingCompleted = true
    )
    assertEquals("Ahmed", settings.tutorName)
    assertEquals(60, settings.defaultDurationMinutes)
    assertEquals("QAR", settings.defaultCurrency)
    assertEquals("ar", settings.appLanguage)
    assertTrue(settings.isOnboardingCompleted)
  }

  @Test
  fun testDarsiSimpleV1BilingualTabs() {
    val enStrings = com.example.ui.localization.darsiStrings("en")
    val arStrings = com.example.ui.localization.darsiStrings("ar")

    assertEquals("Today", enStrings.today)
    assertEquals("Calendar", enStrings.calendar)
    assertEquals("Students", enStrings.students)
    assertEquals("Payments", enStrings.payments)

    assertEquals("اليوم", arStrings.today)
    assertEquals("التقويم", arStrings.calendar)
    assertEquals("الطلاب", arStrings.students)
    assertEquals("المدفوعات", arStrings.payments)
  }

  @Test
  fun testArabicCountdownFormatting() {
    val now = System.currentTimeMillis()
    val futureIn45Min = now + (45 * 60 * 1000L)
    val end = futureIn45Min + (60 * 60 * 1000L)
    val countdown = DateTimeUtils.getCountdownString(futureIn45Min, end, isArabic = true)
    assertTrue("Should be Arabic countdown: $countdown", countdown.contains("خلال") || countdown.contains("د"))
  }
}

