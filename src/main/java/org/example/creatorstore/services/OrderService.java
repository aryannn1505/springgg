package org.example.creatorstore.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.example.creatorstore.dto.OrderItemRequest;
import org.example.creatorstore.dto.OrderRequest;
import org.example.creatorstore.entities.Order;
import org.example.creatorstore.entities.OrderItem;
import org.example.creatorstore.entities.Product;
import org.example.creatorstore.repositories.OrderRepository;
import org.example.creatorstore.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order creatorOrder(OrderRequest orderRequest){
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setStatus("CONFIRMED");

        for (OrderItemRequest itemRequest : orderRequest.getItems()){
            Product product = productRepository.findById(
                    itemRequest.getProductId()
            ).orElseThrow(()-> new RuntimeException(
                    "Product not found with id " + itemRequest.getProductId()
            ));

            //Check product stock
            if(product.getStockQuantity()< itemRequest.getQuantity()){
                throw new RuntimeException("Not enough stock for "+ itemRequest);
            }

            //Calculate total price
            BigDecimal priceOfItem = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            totalPrice = totalPrice.add(priceOfItem);

            //Update the product table with latest stock quantity
            product.setStockQuantity(
                    product.getStockQuantity()- itemRequest.getQuantity()
            );
            productRepository.save(product);

            //Builder pattern to make obj
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            orderItems.add(orderItem);
        }
        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);
        return orderRepository.save(order);

    }

    //Get all orders
    public List<Order> getAllOrders(){
        //TODO: to be implemented
        return null;
    }

    //Get order by id
    public Order getOrderById(){
        //TODO: to be implemented
        return null;
    }
}
