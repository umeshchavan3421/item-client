package com.reactiveclient.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactiveclient.domain.Item;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class ItemClientController {
	
	WebClient webClient = WebClient.create("http://localhost:8080");

	@GetMapping("/cleient/exchange")
	public Flux<Item> getAllItemsUsingExchange(){
		
		//Using exchange method we will get the client response along with the status
		//we have to extract response  from it
		return webClient.get().uri("/v1/items")
		.exchange()
		.flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
		.log("Items in Client project using exchange");
	}
	
	@GetMapping("/cleient/retrive")
	public Flux<Item> getAllItemsUsingRetrive(){
		//Retrive will give the response body directly
		
		return webClient.get().uri("/v1/items")
		.retrieve()
		.bodyToFlux(Item.class)
		.log("Items in Client project using retrive");
	}
	
	@GetMapping("/cleient/retrive/singleItem")
	public Mono<Item> getOneItemsUsingRetrive(){
		
		String id = "mobile";
		
		return webClient.get().uri("/v1/item/one/{id}", id)
		.retrieve()
		.bodyToMono(Item.class)
		.log("Items in Client project using retrive single item");
	}
	
	@GetMapping("/cleient/exchange/singleItem")
	public Mono<Item> getOneItemsUsingExchange(){
		
		String id = "mobile";
		
		return webClient.get().uri("/v1/item/one/{id}", id)
		.exchange()
		.flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
		.log("Items in Client project using retrive single item");
	}
	
	@PostMapping("/client/create/item")
	public Mono<Item> createItem(@RequestBody Item item){
		
		Mono<Item> itemReq = Mono.just(item);
		
		return webClient.post().uri("/v1/item/add")
				.contentType(MediaType.APPLICATION_JSON)
				.body(itemReq, Item.class)
				.retrieve()
				.bodyToMono(Item.class)
				.log("Item created successfully :");
	}
	
	@PutMapping("/client/update/item")
	public Mono<Item> updateItem(@PathVariable String id, @RequestBody Item item){
		
		Mono<Item> itemReq = Mono.just(item);
		
		return webClient.put().uri("/v1/item/update/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(itemReq, Item.class)
				.retrieve()
				.bodyToMono(Item.class)
				.log("Item updated successfully :");
	}
	
	@PutMapping("/client/update/item/rem")
	public Mono<Item> deleteItem(@PathVariable String id){
		
		//Change required in source for pathVariable
		
		return webClient.put().uri("/v1/item/delete", id)
				.contentType(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Item.class)
				.log("Item updated successfully :");
	}
	
	
	@GetMapping("/cleient/retrive/error")
	public Flux<Item> errorRetruve(){
		
		
		return webClient.get().uri("/v1/item/exception")
		.retrieve()
		.onStatus(HttpStatus::is5xxServerError, clientResponse ->{
			Mono<String> errorMono = clientResponse.bodyToMono(String.class);
			
			return errorMono.flatMap((errorMsg) ->{
				log.error("Error message is" +errorMsg);
				throw new RuntimeException(errorMsg);
			});
		})
		.bodyToFlux(Item.class);
	}
}
