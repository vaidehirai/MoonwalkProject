package com.vaidehi.moonwalk.service;

import com.vaidehi.moonwalk.entity.Order;
import com.vaidehi.moonwalk.repository.OrderRepository;
import com.vaidehi.moonwalk.request.OrderRequest;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.response.ItemResponse;
import com.vaidehi.moonwalk.response.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ItemService itemService;

    //Initialising the ArrayList containing all the chef queues. Each chef queue is a Deque containing the orders assigned to that chef.
    static int totalChefs;
    static ArrayList<Deque<Order>> chefQueues=new ArrayList<>();

    public OrderService(OrderRepository orderRepository, ItemService itemService) {
        this.orderRepository = orderRepository;
        this.itemService = itemService;
    }

    public ResponseEntity<APIResponse> createOrder(@RequestBody OrderRequest orderRequest){
        log.info("Creating order {} with {} items", orderRequest.getOrderId(), orderRequest.getItemIds().size());
        totalChefs=chefQueues.size();
        removingInactiveOrdersFromChefQueues();

        try {
            //Retrieving all items in an order
            ArrayList<Long> itemsInAnOrder=orderRequest.getItemIds();
            //Considering each item as different orders for different chefs to complete
            for(int i=0;i<itemsInAnOrder.size();i++){
                Order order=new Order();
                order.setOrderId(orderRequest.getOrderId());
                order.setItemId(itemsInAnOrder.get(i));
                log.debug("Processing item {} for order {}", itemsInAnOrder.get(i), orderRequest.getOrderId());
                allotingOrdersToChefAndCalculatingTimeWhenReady(order);
                orderRepository.save(order);
            }

            log.info("Order {} created successfully", orderRequest.getOrderId());
            return ResponseEntity.status(HttpStatus.OK).body(new APIResponse("Order created successfully"));
        } catch (DataAccessException exception) {
            log.error("Failed to create order {}", orderRequest.getOrderId(), exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse("Failed to create order"));
        }
    }

    public ResponseEntity<APIResponse> getActiveOrders(){
        try {
            log.info("Fetching active orders");
            List<Order> allOrders= orderRepository.findAll();

            //Filtering out the active orders i.e the orders whose timeWhenOrderReady is after the current time
            List<Order> activeOrders=allOrders.stream().filter(order -> order.getTimeWhenOrderReady().isAfter(LocalTime.now())).collect(Collectors.toList());

            if(activeOrders.isEmpty()){
                log.warn("No active orders found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new APIResponse("No active orders"));
            }
            else{
                OrderResponse orderResponse=new OrderResponse();
                orderResponse.setAllOrders(activeOrders);
                orderResponse.setMsg("Active orders fetched successfully");
                log.info("Fetched {} active orders", activeOrders.size());
                return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
            }
        } catch (DataAccessException exception) {
            log.error("Failed to fetch active orders", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse("Failed to fetch active orders"));
        }
    }


    private void allotingOrdersToChefAndCalculatingTimeWhenReady(Order order){
        //Retrieving the preparation time of the item present in the order
        Long itemId=order.getItemId();
        ResponseEntity<APIResponse> response=itemService.getItemById(itemId);
        ItemResponse itemResponse=(ItemResponse)response.getBody();
        int preparationTime=itemResponse.getItem().getPreparationTime();
        log.debug("Preparation time for item {} is {} minutes", itemId, preparationTime);

        //Allot orders to chefs whose queue is empty
        for(int i=0;i<chefQueues.size();i++){
            if(chefQueues.get(i).isEmpty()){
                LocalTime totalTime=LocalTime.now().plusMinutes((long)preparationTime);
                order.setTimeWhenOrderReady(totalTime);
                chefQueues.get(i).add(order);
                log.debug("Assigned item {} to empty chef queue {}. Ready at {}", itemId, i, totalTime);
                return;
            }
        }

        //Finding the chef whose queue has the order that will be ready the earliest
        LocalTime minTime=chefQueues.get(0).peekLast().getTimeWhenOrderReady();
        int chefQueueNo=0;
        for(int i=1;i<chefQueues.size();i++){
            if(chefQueues.get(i).peekLast().getTimeWhenOrderReady().isBefore(minTime)){
                minTime=chefQueues.get(i).peekLast().getTimeWhenOrderReady();
                chefQueueNo=i;
            }
        }

        //Calculating the time when the order will be ready and adding it to the chef's queue
        LocalTime orderCompletionTime=chefQueues.get(chefQueueNo).peekLast().getTimeWhenOrderReady().plusMinutes((long)preparationTime);
        order.setTimeWhenOrderReady(orderCompletionTime);
        chefQueues.get(chefQueueNo).add(order);
        log.debug("Assigned item {} to chef queue {}. Ready at {}", itemId, chefQueueNo, orderCompletionTime);

    }

    //Removing inactive orders so that if there are no orders for sometime, empty chef queues are present again for inserting orders
    private void removingInactiveOrdersFromChefQueues(){
        log.debug("Removing inactive orders from chef queues");
        for(int i=0;i<chefQueues.size();i++){
            for(int j=0;j<chefQueues.get(i).size();j++){
                if(chefQueues.get(i).peek().getTimeWhenOrderReady().isBefore(LocalTime.now())){
                    chefQueues.get(i).poll();
                }
                else{
                    break;
                }
            }
        }

    }


}
