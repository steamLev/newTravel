package todo.demo.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import todo.demo.Models.OrderTask;
import todo.demo.Models.Tour;
import todo.demo.Services.TravelService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class TourControllers {


    ExecutorService service=   Executors.newFixedThreadPool(3);
    @Autowired
    TravelService travelService;

    @PostMapping(value = "tour/create/{from}/{to}/{data}/{transport}/{clientId}")
    public void createTour(@PathVariable("from") String from,
                           @PathVariable("to") String to,
                           @PathVariable("data") String data,
                           @PathVariable("transport") Integer transport,
                           @PathVariable("clientId") Integer clientId){
        Boolean result;
//создаем задание
        try{
            Callable<Boolean> callableTask = () -> {return  travelService.createTour(new Tour(from,to, data,transport,clientId));};
            result=service.submit(callableTask).get(3000, TimeUnit.SECONDS);

        }
        catch (Exception e){log.info("deleteTask error");e.printStackTrace();}

    }

    @GetMapping(value = "tour/get/all")
    public List<?> getTour(){
        List<Tour> list=new ArrayList<>();
        //выгружаем все  задания в многопоточном режиме
        try{
            Callable<List<Tour>> callableTask = () -> (List<Tour>) travelService.getTour( new Tour());
            list=service.submit(callableTask).get(3000, TimeUnit.SECONDS);
        }
        catch (Exception e){log.info("getTask error");
            e.printStackTrace();}

        return list;
    }

    @GetMapping(value = "tour/get/{id}")
    public  Tour getTourId(@PathVariable("id") Long id){
        Tour tour=new Tour();

        //выгружаем    задание по id
        try{
            tour.setId(id);
            Tour finalTour = tour;
            Callable<Tour> callableTask = () -> ( Tour) travelService.getTour(finalTour);
            tour=service.submit(callableTask).get(3000, TimeUnit.SECONDS);
        }
        catch (Exception e){log.info("getTaskId error");e.printStackTrace();}
        return tour;
    }

    @DeleteMapping(value = "tour/delete/{id}")
    public void deleteTour(@PathVariable("id") Long id){
        Tour tour=new Tour();
        Boolean result;
        //удаляем    задание по id
        try{
            tour.setId(id);
            Callable<Boolean> callableTask = () -> travelService.deleteTour(tour);
            result=service.submit(callableTask).get(3000, TimeUnit.SECONDS);
        }
        catch (Exception e){log.info("deleteTask error");e.printStackTrace();}
    }

    @PutMapping(value = "tour/put/{id}/{from}/{to}/{date}/{transport}")
    public void putTour(@PathVariable("id") Long id,
                        @PathVariable("from") String from,
                        @PathVariable("to") String to,
                        @PathVariable("date") String date,
                        @PathVariable("transport") Integer transport){
        Tour tour=new Tour(id,from,to,date,transport);
        Boolean result;
        //вносим изменения в     задание по id
        try{
            Callable<Boolean> callableTask = () -> travelService.updateTour(tour);
            result=service.submit(callableTask).get(3000, TimeUnit.SECONDS);
        }
        catch (Exception e){log.info("putTask error");e.printStackTrace();}


    }
}
