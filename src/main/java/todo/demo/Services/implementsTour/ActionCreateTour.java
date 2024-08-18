package todo.demo.Services.implementsTour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import todo.demo.Models.Tour;
import todo.demo.Repositories.TourRepository;
import todo.demo.Services.Action;

import java.math.BigDecimal;

@Component(value = "createTour")
public class ActionCreateTour implements Action {
    Tour tour;
@Autowired
TourRepository tourRepository;

    @Override
    public Object act(Object object) {
        tour=(Tour) object;
        tour.setStatus(true);
        tour.setAmount(new BigDecimal("233"));
        tourRepository.save(tour);
        return  true;
    }

}
