package todo.demo.Services.implementsTour;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import todo.demo.Models.OrderTask;
import todo.demo.Models.Tour;
import todo.demo.Repositories.OrderRepository;
import todo.demo.Repositories.TourRepository;
import todo.demo.Services.Action;

@Component(value = "updateTour")
public class ActionUpdateTour implements Action {
   Tour tour;
    @Autowired
    TourRepository tourRepository;

    @Override
    public Object act(Object object) {
        tour=(Tour) object;
        tourRepository.deleteById(tour.getId());
        tourRepository.save(tour);
        return  true;
    }

}