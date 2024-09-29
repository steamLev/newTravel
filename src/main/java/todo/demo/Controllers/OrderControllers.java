package todo.demo.Controllers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import todo.demo.Models.OrderTask;
import todo.demo.Models.PageHtml;
import todo.demo.Services.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Controller
public class OrderControllers {

ExecutorService service=   Executors.newFixedThreadPool(3);
@Autowired
TravelService travelService;

    @Value("${processType.create}")
    String create;
    @Value("${processType.process}")
    String processed;
    @Value("${processType.done}")
    String done;

    @Autowired
    @Qualifier("GenerateWebTravel")
    WebGenerate webGenerate;
    @GetMapping("/order")
    public String indexOrder(Model model){

        List<PageHtml> generatedPages = webGenerate.getGeneratedPages();
        model.addAttribute("generatedPages", generatedPages);
        return "index";
    }
    @GetMapping("/order/destination")
    public String destinationOrder(){

        return "clients/destination";
    }
@PostMapping(value = "order/create/{from}/{to}/{data}/{transport}/{clientId}")
public void createTask(@PathVariable("from") String from,
                       @PathVariable("to") String to,
                       @PathVariable("data") String data,
                       @PathVariable("transport") Integer transport,
                       @PathVariable("clientId") Integer clientId,
                       @PathVariable("amount") Integer amount){
Boolean result;
//создаем задание
    try{
        Callable<Boolean> callableTask = () -> {
            return  travelService.createOrder(new OrderTask(from,to, data,transport,clientId,new BigDecimal(amount)));};
        result=service.submit(callableTask).get(3000, TimeUnit.SECONDS);

    }
    catch (Exception e){log.info("deleteTask error");e.printStackTrace();}

}

    @PostMapping(value = "/order/create")
    public String createTaskIndex(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "name", required = false) String name) {

        Boolean result = false;
        log.info("from: " + from);
        log.info("to: " + to);
        log.info("phone: " + phone);
        log.info("name: " + name);

        try {
            Callable<Boolean> callableTask = () -> travelService.createOrder(new OrderTask(from, to, phone, name));
            result = service.submit(callableTask).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error creating task", e);
        }

        if (result) {
            return "redirect:/order/success";  // Перенаправление на страницу успеха
        } else {
            return "redirect:/order/failure";  // Перенаправление на страницу ошибки
        }
    }
    @GetMapping("/order/success")
    public String orderSuccess() {
        return "clients/success";
    }

    @GetMapping("/order/failure")
    public String orderFailure() {
        return "clients/failure";
    }


    @GetMapping(value = "order/get/all")
public String getTask(Model model){
    List<OrderTask> list=new ArrayList<>();
    //выгружаем все  задания в многопоточном режиме
    try{
        Callable<List<OrderTask>> callableTask = () -> (List<OrderTask>) travelService.getOrderTask( new OrderTask());
           list=service.submit(callableTask).get(3000, TimeUnit.SECONDS);

        model.addAttribute("orders", list);
    }
    catch (Exception e){log.info("getTask error");
         e.printStackTrace();}

    return "subs/index";
}

    @GetMapping(value = "order/get/{id}")
    public  OrderTask getTaskId(@PathVariable("id") Long id){
        OrderTask orderTask=new OrderTask();
        orderTask.setId(id);
        //выгружаем    задание по id
        try{
            OrderTask finalOrderTask = orderTask;
            Callable<OrderTask> callableTask = () -> ( OrderTask) travelService.getOrderTask(finalOrderTask);
            orderTask=service.submit(callableTask).get(3000, TimeUnit.SECONDS);
        }
        catch (Exception e){log.info("getTaskId error");e.printStackTrace();}
        return orderTask;
    }

@DeleteMapping(value = "order/delete/{id}")
public void deleteTask(@PathVariable("id") Long id){
    OrderTask orderTask=new OrderTask();
    Boolean result;
    //удаляем    задание по id
    try{
        orderTask.setId(id);
        Callable<Boolean> callableTask = () -> travelService.deleteOrder(orderTask);
        result=service.submit(callableTask).get(3000, TimeUnit.SECONDS);
    }
    catch (Exception e){log.info("deleteTask error");e.printStackTrace();}
}

@PutMapping(value = "order/put/{id}/{from}/{to}/{date}/{transport}/{process}")
public void putTask(@PathVariable("id") Long id,
                    @PathVariable("from") String from,
                    @PathVariable("to") String to,
                    @PathVariable("date") String date,
                    @PathVariable("transport") Integer transport,
                    @PathVariable("process") Integer process){
    OrderTask orderTask=new OrderTask(id,from,to,date,transport,process==1?
            create:process==2?processed:process==3?done:"empty");
    Boolean result;
    //вносим изменения в     задание по id
    try{
        Callable<Boolean> callableTask = () -> travelService.updateOrder(orderTask);
        result=service.submit(callableTask).get(3000, TimeUnit.SECONDS);
    }
    catch (Exception e){log.info("putTask error");e.printStackTrace();}


}
}
