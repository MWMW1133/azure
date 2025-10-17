//package com.azure.controller.api;
//
//import java.util.List;
//
//import org.springframework.data.domain.PageRequest;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.azure.dto.MessageDTO;
//import com.azure.model.chat.Message;
//import com.azure.repository.MessageRepository;
//
//@RestController
//@RequestMapping("/api/messages")
//public class MessageApiController {
//
//    private final MessageRepository messageRepo;
//    public MessageApiController(MessageRepository messageRepo) { this.messageRepo = messageRepo; }
//
//    // 최신 N개 페이지네이션(기본 100개) - 팀 레포에 이미 pageable 메서드가 있음
//    @GetMapping("/channels/{channelId}")
//    public List<MessageDTO> listByChannel(
//            @PathVariable Long channelId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "100") int size
//    ) {
//        // 기존 리포 메서드: findByChannel_IdOrderByIdAsc(Long, Pageable)
//        var pageReq = PageRequest.of(page, size);
//        return messageRepo.findByChannel_IdOrderByIdAsc(channelId, pageReq)
//                .map(this::toDto)
//                .getContent();
//    }
//
//    private MessageDTO toDto(Message m) {
//        MessageDTO dto = new MessageDTO();
//        dto.setId(m.getId());
//        dto.setChannelId(m.getChannel().getId());
//        dto.setAuthorId(m.getAuthor() != null ? m.getAuthor().getId() : null);
//        dto.setBody(m.getBody());
//        dto.setCreatedAt(m.getCreatedAt());
//        return dto;
//    }
//}
package com.azure.controller.api;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azure.dto.MessageDTO;
import com.azure.model.chat.Message;
import com.azure.repository.MessageRepository;

@RestController
@RequestMapping("/api/messages")
public class MessageApiController {

    private final MessageRepository messageRepo;
    public MessageApiController(MessageRepository messageRepo) { this.messageRepo = messageRepo; }

    // 최신 N개 페이지네이션(기본 100개) - 팀 레포에 이미 pageable 메서드가 있음
    @GetMapping("/channels/{channelId}")
    public List<MessageDTO> listByChannel(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")); // 최신 먼저
        var pageData = messageRepo.findByChannel_Id(channelId, pageable);
        var list = pageData.getContent().stream().map(this::toDto).toList();

        // 레거시 클라가 오래→최신으로 그린다면 주석 해제해서 뒤집어 주세요.
        // Collections.reverse(list);

        return list;
    }

    private MessageDTO toDto(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setChannelId(m.getChannel().getId());
        dto.setAuthorId(m.getAuthor() != null ? m.getAuthor().getId() : null);
        dto.setBody(m.getBody());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}