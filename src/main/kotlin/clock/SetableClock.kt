package clock

import java.time.Duration
import java.time.Instant

class SetableClock(private var now: Instant) : Clock {
    fun setNow(now: Instant) {
        this.now = now
    }

    fun add(duration: Duration): SetableClock {
        setNow(this.now.plus(duration))
        return this
    }

    override fun now(): Instant {
        return now
    }
}