import clock.SetableClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import statistic.EventsStatistic
import statistic.EventsStatisticImpl
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.Duration
import java.time.Instant

class EventsStatisticImplTest {
    private lateinit var clock: SetableClock
    private lateinit var eventsStatistic: EventsStatistic

    @BeforeEach
    fun setUp() {
        clock = SetableClock(Instant.now())
        eventsStatistic = EventsStatisticImpl(clock)
    }

    @Test
    fun incEvent() {
        eventsStatistic.incEvent("event")
        assertEquals(1.0 / 60, eventsStatistic.getEventStatisticByName("event"))
    }

    @Test
    fun deletingOld() {
        eventsStatistic.incEvent("event")
        clock.add(Duration.ofHours(1)).add(Duration.ofMillis(1))
        assertEquals(0.0, eventsStatistic.getEventStatisticByName("event"))
    }

    @Test
    fun nonExistentEvent() {
        assertEquals(0.0, eventsStatistic.getEventStatisticByName("event"))
    }

    @Test
    fun severalEvents() {
        repeat(15) { eventsStatistic.incEvent("event1") }
        repeat(100) { eventsStatistic.incEvent("event2") }
        repeat(60) { eventsStatistic.incEvent("event3") }
        assertEquals(
            mapOf("event1" to 15.0 / 60, "event2" to 100.0 / 60, "event3" to 60.0 / 60),
            eventsStatistic.getAllEventStatistic()
        )
    }

    @Test
    fun severalEventsInDifferentTime() {
        repeat(15) { eventsStatistic.incEvent("event1") }
        assertEquals(mapOf("event1" to 15.0 / 60), eventsStatistic.getAllEventStatistic())

        clock.add(Duration.ofMinutes(30))
        repeat(100) { eventsStatistic.incEvent("event2") }
        assertEquals(
            mapOf("event1" to 15.0 / 60, "event2" to 100.0 / 60),
            eventsStatistic.getAllEventStatistic()
        )

        clock.add(Duration.ofMinutes(61))
        repeat(60) { eventsStatistic.incEvent("event3") }
        assertEquals(mapOf("event3" to 60.0 / 60), eventsStatistic.getAllEventStatistic())
    }

    @Test
    fun printStatistic() {
        val outputStream = ByteArrayOutputStream()
        repeat(15) { eventsStatistic.incEvent("event1") }
        repeat(30) { eventsStatistic.incEvent("event2") }
        repeat(60) { eventsStatistic.incEvent("event3") }
        repeat(120) { eventsStatistic.incEvent("event4") }
        PrintStream(outputStream).use {
            System.setOut(it)
            eventsStatistic.printStatistic()
        }
        assertEquals(
            "event1 : 0.25\n" +
                    "event2 : 0.5\n" +
                    "event3 : 1.0\n" +
                    "event4 : 2.0\n",
            outputStream.toString()
        )
    }
}