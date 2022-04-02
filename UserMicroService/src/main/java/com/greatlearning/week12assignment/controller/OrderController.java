package com.greatlearning.week12assignment.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greatlearning.week12assignment.config.SwaggerConfig;
import com.greatlearning.week12assignment.exception.ItemNotFoundException;
import com.greatlearning.week12assignment.model.Item;
import com.greatlearning.week12assignment.model.Order;
import com.greatlearning.week12assignment.model.OrderBillWrapper;
import com.greatlearning.week12assignment.model.OrderItem;
import com.greatlearning.week12assignment.model.OrderItemDto;
import com.greatlearning.week12assignment.model.OrdersView;
import com.greatlearning.week12assignment.model.User;
import com.greatlearning.week12assignment.repository.OrderViewRepository;
import com.greatlearning.week12assignment.response.ItemResponse;
import com.greatlearning.week12assignment.service.ItemService;
import com.greatlearning.week12assignment.service.OrderItemService;
import com.greatlearning.week12assignment.service.OrderService;
import com.greatlearning.week12assignment.service.UserService;

import io.swagger.annotations.Api;

@Api(tags = { SwaggerConfig.OrderController_TAG })
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	OrderService orderService;

	@Autowired
	OrderItemService orderItemService;

	@Autowired
	ItemService itemService;

	@Autowired
	UserService userService;
	
	@Autowired
	OrderViewRepository orderViewRepository;
	
	
	
	/*
	 * START
	 * Code for WEEK-11 assignment see all orders using views and filter using views of SQL
	 * */
	
	@GetMapping
	public ResponseEntity<Iterable<OrdersView>> getAllOrdersForCurrentLoggedInUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findByEmail(auth.getName());
		List<OrdersView> orderViews = orderViewRepository.findAllByEmail(user.getEmail());
		return new ResponseEntity<>(orderViews, HttpStatus.OK);
	}
	
	
	@GetMapping("/filterDate")
	public ResponseEntity<Iterable<OrdersView>> getAllOrderFilteringDate(@RequestParam(name = "date", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findByEmail(auth.getName());
		LocalDate date = fromDate.toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDate();
		List<OrdersView> orderViews = orderViewRepository.findAllByEmailAndDateCreated(user.getEmail(), date);
		return new ResponseEntity<>(orderViews, HttpStatus.OK);
	}
	
	@GetMapping("/filterPrice")
	public ResponseEntity<Iterable<OrdersView>> getAllOrderFilteringDate(@RequestParam(name = "price", required = true) Double price){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findByEmail(auth.getName());
		List<OrdersView> orderViews = orderViewRepository.findAllByEmailAndPrice(user.getEmail(), price);
		return new ResponseEntity<>(orderViews, HttpStatus.OK);
	}
	
	
	
	/*
	 * END 
	 * Code for WEEK-11 assignment see all orders using views and filter using views of SQL -- view name  - ALL_ORDERS_FOR_CURRENT_USER
	 * */
	
	

	@PostMapping
	@Transactional
	public ResponseEntity<OrderBillWrapper> generateBill(@RequestBody OrderForm form) {
		List<OrderItemDto> formDtos = form.getItemOrders();

		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findByEmail(auth.getName());

		validateProductsExistence(formDtos);
		Order order = new Order();
		user.addOrder(order);
		order = this.orderService.create(order);

		List<OrderItem> orderItems = new ArrayList<>();
		for (OrderItemDto dto : formDtos) {
			orderItems.add(orderItemService
					.create(new OrderItem(order, itemService.findItemById(dto.getItem().getId()), dto.getQuantity())));
		}
		order.setOrderItems(orderItems);
		this.orderService.update(order);

		String uri = ServletUriComponentsBuilder.fromCurrentServletMapping().path("/orders/{id}")
				.buildAndExpand(order.getId()).toString();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", uri);
		
		
		List<ItemResponse> items = new ArrayList<>();
		order.getOrderItems().stream().forEach(orderItem -> {
			Item item = orderItem.getItem();
			items.add(ItemResponse.builder().name(item.getName()).price(item.getPrice()).quantity(orderItem.getQuantity()).build());
		});

		return new ResponseEntity<>(OrderBillWrapper.builder().bill(order.getTotalOrderPrice()).items(items).build(), headers, HttpStatus.CREATED);
	}

	public static class OrderForm {

		private List<OrderItemDto> itemOrders;

		public List<OrderItemDto> getItemOrders() {
			return itemOrders;
		}

		public void setItemOrders(List<OrderItemDto> itemOrders) {
			this.itemOrders = itemOrders;
		}
	}

	private void validateProductsExistence(List<OrderItemDto> orderProducts) {
		List<OrderItemDto> list = orderProducts.stream()
				.filter(op -> Objects.isNull(itemService.findItemById(op.getItem().getId())))
				.collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(list)) {
			new ItemNotFoundException("Item not found");
		}
	}
}
