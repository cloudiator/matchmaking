package choco;

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
}
