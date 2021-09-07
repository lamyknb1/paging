package com.t3h.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.t3h.entity.Room;
import com.t3h.entity.Student;
import com.t3h.model.ResponseObj;
import com.t3h.service.T3hRoomService;

@RestController
@RequestMapping(value = "/room")
public class RoomController {
	
	@Autowired
	private T3hRoomService service;
	
	
	@Transactional
	@RequestMapping(value = "/insertRoom", method = RequestMethod.POST)
	public String insertRoom(
			@RequestParam(name = "roomCode", defaultValue = "") String roomCode,
			@RequestParam(name = "location", defaultValue = "") String location) {
		
		Room r =new Room();
		r.setRoomCode(roomCode);
		r.setLocation(location);
		service.save(r);
		
		return "OK";
		
	}
	
	
	@Transactional
	@RequestMapping(value = "/updateRoom", method = RequestMethod.POST)
	public String updateRoom(
			@RequestParam(name = "roomId", defaultValue = "") Long roomId,
			@RequestParam(name = "roomCode", defaultValue = "") String roomCode,
			@RequestParam(name = "location", defaultValue = "") String location) {
		
		Optional<Room> op = service.findById(roomId);
		if (!op.isPresent()) {			
			return "Fail";
		}
		Room t = op.get();
		t.setLocation(location);
		t.setRoomCode(roomCode);
		service.save(t);
		
		return "OK";
		
	}
	
	
	
	@Transactional
	@RequestMapping(value = "/updateByRoomCode", method = RequestMethod.POST)
	public String updateByRoomCode(
			@RequestParam(name = "roomCode", defaultValue = "") String roomCode,
			@RequestParam(name = "newLocation", defaultValue = "") String newLocation) {
		
		service.updateByRoomCode(roomCode, newLocation);
		
		return "OK";
		
	}
	
	@Transactional
	@RequestMapping(value = "/deleteRoomById", method = RequestMethod.POST)
	public String deleteRoomById(
			@RequestParam(name = "roomId", defaultValue = "") Long roomId) {
		
		Optional<Room> op = service.findById(roomId);
		if (!op.isPresent()) {			
			return "Fail";
		}
		Room t = op.get();

		service.delete(t);
		return "OK";
		
	}

}
