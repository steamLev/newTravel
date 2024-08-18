package todo.demo.Services.implementsOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import todo.demo.Models.OrderTask;
import todo.demo.Repositories.OrderRepository;
import todo.demo.Services.Action;

import java.util.Optional;


@Component(value = "get")
public class ActionGetOrder implements Action {
    OrderTask order;
    @Autowired
    OrderRepository orderRepository;

    @Override
    public Object act(Object object) {
        order=(OrderTask)object;

        if(Optional.ofNullable(order.getId()).isEmpty()){
            return orderRepository.findAll();}
        else {
            return orderRepository.findById(order.getId()) .get();
        }
    }

}
