package todo.demo.Services.implementsOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import todo.demo.Models.OrderTask;
import todo.demo.Repositories.OrderRepository;
import todo.demo.Services.Action;


@Component(value = "delete")
public class ActionDeleteOrder implements Action {
    OrderTask order;
    @Autowired
    OrderRepository orderRepository;

    @Override
    public Object act(Object object) {
        order=(OrderTask)object;
        orderRepository.deleteById(order.getId());
        return  true;
    }

}