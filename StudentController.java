package com.t3h.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.t3h.config.AppConfig;
import com.t3h.dto.StudentDto;
import com.t3h.entity.Room;
import com.t3h.entity.Student;
import com.t3h.model.SearchParam;
import com.t3h.service.StudentService;
import com.t3h.service.T3hRoomService;
import com.t3h.service.impl.StudentServiceImpl;

@Controller
@RequestMapping(value = "/student")
public class StudentController {

	private Logger logger = Logger.getLogger(getClass());

	private final Integer pageSize = 10;

	@Autowired
	private StudentService service;

	@Autowired
	private StudentServiceImpl serviceImpl;

	@Autowired
	private T3hRoomService roomService;

	@Autowired
	private AppConfig appConfig;

	@Transactional
	@RequestMapping(value = { "/list" }, method = RequestMethod.GET)
	public String search(Model model, @RequestParam(name = "name", defaultValue = "") String name,
			@RequestParam(name = "roomId", defaultValue = "") Long roomId,
			@RequestParam(name = "pageIndex", defaultValue = "1") Integer pageIndex) {

		try {

			model.addAttribute("searchName", name);
			model.addAttribute("searchRoomId", roomId);
			List<SearchParam> searchParamLst = new ArrayList<>();
			if (name != null && !name.trim().isEmpty()) {
				searchParamLst.add(new SearchParam("name", name));
			}

			List<Student> studentListList = serviceImpl.searchStudent(name, roomId, pageIndex, pageSize);
			List<StudentDto> dataList = new ArrayList<>();
			for (Student o : studentListList) {
				StudentDto dto = new StudentDto();
				dto.setId(o.getId());
				dto.setAddress(o.getAddress());
				dto.setName(o.getName());
				dto.setImage((o.getImage() != null && !o.getImage().trim().isEmpty())
						? appConfig.getImageUrl() + File.separator + o.getImage().trim()
						: appConfig.getImageUrl() + File.separator + appConfig.getNoImageFileName());
				
				dto.setRoomCode(o.getRoomCode());
				if (o.getBirthDate() != null) {
					dto.setBirthDate(new SimpleDateFormat("dd/MM/yyyy").format(o.getBirthDate()));
				}
				dataList.add(dto);
			}

			model.addAttribute("dataList", dataList);

			Long totalElement = serviceImpl.countStudent(name, roomId);
			Long totalPages = (totalElement % pageSize != 0) ? (totalElement / pageSize + 1)
					: (totalElement / pageSize);
			model.addAttribute("totalPages", totalPages);
			model.addAttribute("currentPage", pageIndex);
			
			int minPage = pageIndex - 2;
			if(minPage<1) {
				minPage =1;
			}
			
			int maxPage = pageIndex +2 ;
			if(maxPage > totalPages) {
				maxPage = totalPages.intValue();
			}

			List<Integer> pageNumbers = IntStream.rangeClosed(minPage, maxPage).boxed()
					.collect(Collectors.toList());
			
			
			model.addAttribute("pageNumbers", pageNumbers);

			if (roomId != null) {
				searchParamLst.add(new SearchParam("roomId", roomId));
			}
			model.addAttribute("searchData", searchParamLst);

			Iterable<Room> roomIterator = roomService.findAll();
			List<Room> roomList = new ArrayList<>();
			roomIterator.forEach(o -> {
				roomList.add(o);
			});
			model.addAttribute("roomList", roomList);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "/student/list";
	}

//	@Transactional
//	@RequestMapping(value = { "/save" }, method = RequestMethod.POST)
//	public String save(Model model, @Valid @ModelAttribute(name = "detailData") StudentDto dto, BindingResult result) {

	
	@Transactional
	@RequestMapping(value = { "/save" }, method = RequestMethod.POST)
	public String save(Model model, @Valid @ModelAttribute(name = "detailData") StudentDto dto, BindingResult result) {
		
		if (result.hasErrors()) {

			model.addAttribute("detailData", dto);
			Iterable<Room> roomIterator = roomService.findAll();
			List<Room> roomList = new ArrayList<>();
			roomIterator.forEach(o -> {
				roomList.add(o);
			});
			model.addAttribute("roomList", roomList);
			return "/student/detail";
		}

		Student student;

		if (Objects.equals(2, dto.getActionType())) {
			Optional<Student> op = service.findById(dto.getId());
			if (!op.isPresent()) {
				result.getAllErrors().add(new ObjectError("ErrorCode", "Object not found"));
				model.addAttribute("detailData", dto);
				return "/student/detail";
			}
			student = op.get();
		} else {
			student = new Student();
		}

		student.setName(dto.getName());
		student.setAddress(dto.getAddress());

		if (dto.getBirthDate() != null) {
			try {
				student.setBirthDate(new SimpleDateFormat("dd/MM/yyyy").parse(dto.getBirthDate()));
			} catch (ParseException e) {
				this.logger.error(e.getMessage(), e);
			}
		}

		if (dto.getRoomId() != null) {
			Room r = new Room();
			r.setRoomId(dto.getRoomId());
			student.setRoom(r);
		}

		if (dto.getUploadedFile() != null && !dto.getUploadedFile().isEmpty()) {
			try {
				byte[] bytes = dto.getUploadedFile().getBytes();

				String newFileName = (new Date().getTime()) + "_" + dto.getUploadedFile().getOriginalFilename();
				Path path = Paths.get(appConfig.getImagefolderPath() + File.separator + newFileName);
				Files.write(path, bytes);

				student.setImage(newFileName);

			} catch (Exception e) {

			}
		}

		service.save(student);

		return "redirect:/student/list";
	}

	@ResponseBody
	@Transactional
	@RequestMapping(value = { "/delete" }, method = RequestMethod.POST)
	public String delete(Model model, @RequestParam(name = "id", required = true) Long id) {

		try {
			Optional<Student> op = service.findById(id);
			if (!op.isPresent()) {
				return "/errorPage";
			}
			service.delete(op.get());

			return "Thành Công";
		} catch (Exception e) {
			logger.error(e, e);
		}
		return "Có lỗi xay ra";

	}

	@Transactional
	@RequestMapping(value = { "/create" }, method = RequestMethod.GET)
	public String create(Model model) {

		Iterable<Room> roomIterator = roomService.findAll();
		List<Room> roomList = new ArrayList<>();
		roomIterator.forEach(o -> {
			roomList.add(o);
		});
		model.addAttribute("roomList", roomList);

		StudentDto dto = new StudentDto();
		dto.setImage(appConfig.getImageUrl() + File.separator + appConfig.getNoImageFileName());
		dto.setActionType(1);
		model.addAttribute("detailData", dto);
		return "/student/detail";

	}

	@Transactional
	@RequestMapping(value = { "/getDetail" }, method = RequestMethod.GET)
	public String getDetail(Model model, @RequestParam(name = "id", required = true) Long id) {

		Optional<Student> op = service.findById(id);
		if (!op.isPresent()) {
			return "/errorPage";
		}

		Student o = op.get();

		StudentDto dto = new StudentDto();
		dto.setId(o.getId());
		dto.setAddress(o.getAddress());
		dto.setName(o.getName());
		dto.setImage((o.getImage() != null && !o.getImage().trim().isEmpty())
				? appConfig.getImageUrl() + File.separator + o.getImage().trim()
				: appConfig.getImageUrl() + File.separator + appConfig.getNoImageFileName());

		if (o.getBirthDate() != null) {
			dto.setBirthDate(new SimpleDateFormat("dd/MM/yyyy").format(o.getBirthDate()));
		}
		if (o.getRoom() != null) {
			dto.setRoomId(o.getRoom().getRoomId());
		}
		dto.setActionType(2);

		model.addAttribute("detailData", dto);

		Iterable<Room> roomIterator = roomService.findAll();
		List<Room> roomList = new ArrayList<>();
		roomIterator.forEach(obj -> {
			roomList.add(obj);
		});
		model.addAttribute("roomList", roomList);

		return "/student/detail";
	}

}
