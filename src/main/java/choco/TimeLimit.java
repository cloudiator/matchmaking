package choco;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

public class TimeLimit {

  public static final TimeLimit INFINITY = new TimeLimit(TimeUnit.DAYS, Long.MAX_VALUE);

  private final TimeUnit timeUnit;
  private final long duration;

  public TimeLimit(TimeUnit timeUnit, long duration) {
    this.timeUnit = timeUnit;
    this.duration = duration;
  }

  public long toMillis() {
    return timeUnit.toMillis(duration);
  }

  @Override
  public String toString() {
    return "TimeLimit{" +
        "timeUnit=" + timeUnit +
        ", duration=" + duration +
        '}';
  }

  public static TimeLimit from(String string) {
    TimeUnit parsedUnit = null;
    for (TimeUnit timeUnit : TimeUnit.values()) {
      if (string.contains(timeUnit.toString())) {
        parsedUnit = timeUnit;
        break;
      }
    }
    checkNotNull(parsedUnit, "Could not parse time unit");
    long duration = Long.valueOf(string.replaceAll("[^0-9]+", " ").trim());
    return new TimeLimit(parsedUnit, duration);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TimeLimit timeLimit = (TimeLimit) o;

    if (duration != timeLimit.duration) {
      return false;
    }
    return timeUnit == timeLimit.timeUnit;
  }

  @Override
  public int hashCode() {
    int result = timeUnit.hashCode();
    result = 31 * result + (int) (duration ^ (duration >>> 32));
    return result;
  }
}
