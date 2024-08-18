package todo.demo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import todo.demo.Models.OrderTask;
import todo.demo.Models.Tour;
import todo.demo.Services.implementsOrder.ActionCreateOrder;

import java.util.List;

@Service
public class TravelService {

    @Autowired
    @Qualifier(value = "create")
    Action actionCreateOrder;

    @Autowired
    @Qualifier(value = "createTour")
    Action actionCreateTour;

    @Autowired
    @Qualifier(value = "update")
    Action actionUpdateOrder;

    @Autowired
    @Qualifier(value = "updateTour")
    Action actionUpdateTour;
    @Autowired
    @Qualifier(value = "delete")
    Action actionDeleteOrder;

    @Autowired
    @Qualifier(value = "deleteTour")
    Action actionDeleteTour;

    @Autowired
    @Qualifier(value = "get")
    Action actionGetOrder;

    @Autowired
    @Qualifier(value = "getTour")
    Action actionGetTour;
    public Boolean createOrder(OrderTask order ){
         actionCreateOrder.act(order);

        return true;
    }

    public Boolean updateOrder(OrderTask order ){
        actionUpdateOrder.act(order);

        return true;
    }

    public Boolean deleteOrder(OrderTask order ){
        actionDeleteOrder.act(order);

        return true;
    }

    public Object getOrderTask(Object orderTask){
        return   actionGetOrder.act(orderTask);

    }

    public Boolean createTour(Tour tour ){
        actionCreateTour.act(tour);

        return true;
    }

    public Boolean updateTour(Tour tour ){
        actionUpdateTour.act(tour);

        return true;
    }

    public Boolean deleteTour(Tour tour ){
        actionDeleteTour.act(tour);

        return true;
    }

    public Object getTour(Tour tour){
        return   actionGetTour.act(tour);

    }
}
