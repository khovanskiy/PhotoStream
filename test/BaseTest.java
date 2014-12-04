import junit.framework.TestSuite;
import org.junit.Test;
import ru.example.PhotoStream.Event;
import ru.example.PhotoStream.EventDispatcher;
import ru.example.PhotoStream.IEventHandler;

public class BaseTest extends TestSuite {
    private class Handler1 implements IEventHandler {

        @Override
        public void handleEvent(Event e) {

        }
    }

    @Test
    public void addTest() {
        EventDispatcher dispatcher = new EventDispatcher();
        Handler1 a = new Handler1();
        Handler1 b = new Handler1();
        Handler1 c = new Handler1();
        dispatcher.addEventListener(a);
        dispatcher.addEventListener(b);
        dispatcher.addEventListener(c);
    }
}
