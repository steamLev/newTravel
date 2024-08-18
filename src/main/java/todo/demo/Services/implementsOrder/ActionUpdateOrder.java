package todo.demo.Services.implementsOrder;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import todo.demo.Models.OrderTask;
import todo.demo.Repositories.OrderRepository;
import todo.demo.Services.Action;
import todo.demo.Services.NotifierHandler;
import todo.demo.Services.PayHandler;

import java.math.BigDecimal;

@Component(value = "update")
public class ActionUpdateOrder implements Action {
    OrderTask order;
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    @Qualifier(value = "megaNotifier")
    NotifierHandler notifierHandler;
    @Autowired
    @Qualifier(value = "megaPay")
    PayHandler payHandler;
    @Value("processType.done")
    String done;

    @Value("notifier.tourdone")
    String notifierTourDone;

    @Override
    public Object act(Object object) {
        order=(OrderTask)object;
        Boolean result;
        if(order.getProcess().equals(done)){
        result=  payHandler.send();

        if(result==true){
            notifierHandler.send(notifierTourDone);
            orderRepository.deleteById(order.getId());
            orderRepository.save(order);
            return true;
        }else{
            return false;}
        }
        orderRepository.deleteById(order.getId());
        orderRepository.save(order);

        return  true;
    }

}