package todo.demo.Services.ImplementsNotifier;


import org.springframework.stereotype.Component;
import todo.demo.Services.NotifierHandler;

@Component(value = "megaNotifier")
public class MegaNotifier implements NotifierHandler {

    @Override
    public Boolean send(String message) {
        return true;
    }
}
