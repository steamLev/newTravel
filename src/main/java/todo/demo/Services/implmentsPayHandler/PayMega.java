package todo.demo.Services.implmentsPayHandler;

import org.springframework.stereotype.Component;
import todo.demo.Services.PayHandler;

@Component(value = "megaPay")
public class PayMega implements PayHandler {


    @Override
    public Boolean send() {
        return true;
    }
}
