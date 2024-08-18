package todo.demo.Services.implementsOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import todo.demo.Models.OrderTask;
import todo.demo.Repositories.OrderRepository;
import todo.demo.Services.Action;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component(value = "create")
public class ActionCreateOrder implements Action {
    OrderTask order;
    @Value("${processType.create}")
    String create;
@Autowired
    OrderRepository orderRepository;

    @Override
    public Object act(Object object) {
        order=(OrderTask)object;
        order.setStatus(false);
        order.setProcess(create);
        order.setScheduledDate(  LocalDate.now().toString());
        orderRepository.save(order);
        return  true;
    }

}
