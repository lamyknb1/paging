package com.t3h.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.t3h.entity.Room;
import com.t3h.entity.Student;
import com.t3h.model.ResponseObj;
import com.t3h.service.T3hRoomService;
import com.t3h.service.StudentService;

@RestController
@RequestMapping(value = "/demo")
public class DemoController {

	private Logger log = Logger.getLogger(getClass());

	@Autowired
	private StudentService studentService;

	//@Autowired
	//private RoomService  roomService;

	private Gson gson = new Gson();

	@Transactional
	@RequestMapping(value = "/saveStudent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseObj saveStudent(@RequestParam(name = "name", defaultValue = "") String name,
			@RequestParam(name = "birthDate", defaultValue = "") String birthDateValue,
			@RequestParam(name = "address", defaultValue = "") String address,
			@RequestParam(name = "roomId", defaultValue = "") Long roomId) {

		ResponseObj rp = new ResponseObj();
		rp.setErrorCode(ResponseObj.SUCCESS_CODE);
		try {

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date birthDate = null;
			if (birthDateValue != null && !birthDateValue.trim().isEmpty()) {
				birthDate = df.parse(birthDateValue);
			}

			Student t = new Student();
			t.setName(name);
			t.setBirthDate(birthDate);
			t.setAddress(address);

			if (roomId != null) {
				Room r = new Room();
				r.setRoomId(roomId);
				t.setRoom(r);
			}

			studentService.save(t);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rp.setErrorCode(ResponseObj.FAIL_CODE);
		}

		return rp;

	}

	@Transactional
	@RequestMapping(value = "/updateStudent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseObj updateStudent(@RequestParam(name = "id", defaultValue = "") Long id,
			@RequestParam(name = "name", defaultValue = "") String name,
			@RequestParam(name = "birthDate", defaultValue = "") String birthDateValue,
			@RequestParam(name = "address", defaultValue = "") String address,
			@RequestParam(name = "roomId", defaultValue = "") Long roomId) {

		ResponseObj rp = new ResponseObj();
		rp.setErrorCode(ResponseObj.SUCCESS_CODE);
		try {

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date birthDate = null;
			if (birthDateValue != null && !birthDateValue.trim().isEmpty()) {
				birthDate = df.parse(birthDateValue);
			}

			Optional<Student> op = studentService.findById(id);
			if (!op.isPresent()) {
				rp.setErrorCode(ResponseObj.FAIL_CODE);
				return rp;
			}
			Student t = op.get();

			t.setName(name);
			t.setBirthDate(birthDate);
			t.setAddress(address);

			if (roomId != null) {
				Room r = new Room();
				r.setRoomId(roomId);
				t.setRoom(r);
			}

			studentService.save(t);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rp.setErrorCode(ResponseObj.FAIL_CODE);
		}

		return rp;

	}

	@Transactional
	@RequestMapping(value = "/deleteStudent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseObj deleteStudent(@RequestParam(name = "id", defaultValue = "") Long id) {

		ResponseObj rp = new ResponseObj();
		rp.setErrorCode(ResponseObj.SUCCESS_CODE);
		try {

			Optional<Student> op = studentService.findById(id);
			if (!op.isPresent()) {
				rp.setErrorCode(ResponseObj.FAIL_CODE);
				return rp;
			}

			Student t = op.get();
			studentService.delete(t);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rp.setErrorCode(ResponseObj.FAIL_CODE);
		}

		return rp;

	}

	@Transactional
	@RequestMapping(value = "/deleteObjByName", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseObj deleteObj(@RequestParam(name = "name", required = true) String name) {

		ResponseObj rp = new ResponseObj();
		rp.setErrorCode(ResponseObj.SUCCESS_CODE);
		try {
			studentService.deleteObjByName(name);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rp.setErrorCode(ResponseObj.FAIL_CODE);
		}

		return rp;

	}

	@Transactional
	@RequestMapping(value = "/getAllStudent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Student> getAllStudent() {
		List<Student> result = new ArrayList<>();
		try {

			Iterable<Student> itr = studentService.findAll();
			for (Student o : itr) {
				result.add(o);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	@RequestMapping(value = "/getStudentByRoomCode", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Student> getStudentByRoomCode(@RequestParam(name = "roomCode", defaultValue = "") String roomCode) {
		List<Student> result = new ArrayList<>();
		try {

			Iterable<Student> itr = studentService.getStudentByRoomCode(roomCode);
			for (Student o : itr) {
				result.add(o);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	@RequestMapping(value = "/getStudentWithPaging", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Student> getStudentWithPaging(@RequestParam(name = "roomCode", defaultValue = "") String roomCode,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		List<Student> result = new ArrayList<>();
		try {

			Sort sort = Sort.by(Order.desc("id"));
			Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

			Page<Student> page = studentService.getStudentWithPaging(roomCode, pageable);
			result.addAll(page.toList());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

//	@Transactional
//	@RequestMapping(value = "/getAllRoom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//	public List<Room> getAllRoom() {
//		List<Room> result = new ArrayList<>();
//		try {
//
//			Iterable<Room> itr = roomService.findAll();
//			for (Room o : itr) {
//				result.add(o);
//			}
//
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//		return result;
//	}

}
