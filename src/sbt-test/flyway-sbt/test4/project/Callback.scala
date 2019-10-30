import org.flywaydb.core.api.callback.{BaseCallback, Context, Event}

object Callback extends BaseCallback {
  def handle(event: Event, context: Context): Unit = ()
}