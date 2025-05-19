package com.example.demo_tttn.controller;

import com.example.demo_tttn.config.UploadProperties;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/calendar")
public class GoogleCalendarController {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "ICS Calendar Import";

    @Autowired
    private OAuth2AuthorizedClientService clientService;
    @Autowired
    private UploadProperties uploadProperties;

    @PostMapping("/import-ics")
    public String importIcsFile(
            @RequestParam("file") MultipartFile file,
            Model model,
            RedirectAttributes redirectAttributes,
            OAuth2AuthenticationToken authentication) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("icsError", "File ICS không được để trống");
                return "upload";
            }

            // Parse ICS file
            List<VEvent> events = parseICS(file.getInputStream());
            if (events.isEmpty()) {
                model.addAttribute("icsError", "File ICS không chứa sự kiện nào");
                return "upload";
            }

            // Get Google Calendar service
            Calendar service = getCalendarService(authentication);

            // Insert events
            int successCount = 0;
            for (VEvent event : events) {
                try {
                    insertEventToGoogle(service, event);
                    successCount++;
                } catch (Exception e) {
                    System.out.println("Lỗi khi thêm sự kiện: " +
                            (event.getSummary() != null ? event.getSummary().getValue() : "Không có tiêu đề") +
                            " - " + e.getMessage());
                }
            }

            String message = String.format("Đã thêm thành công %d/%d sự kiện vào Google Calendar",
                    successCount, events.size());
            redirectAttributes.addFlashAttribute("icsMessage", message);
            return "redirect:/api/schedule/upload";

        } catch (Exception e) {
            System.out.println("Lỗi xử lý file ICS: " + e.getMessage());
            model.addAttribute("icsError", "Lỗi xử lý file ICS: " + e.getMessage());
            return "upload";
        }
    }

    private List<VEvent> parseICS(
            InputStream inputStream)
            throws Exception {
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = builder.build(inputStream);

        List<VEvent> events = new ArrayList<>();
        for (Object component : calendar.getComponents(Component.VEVENT)) {
            events.add((VEvent) component);
        }
        return events;
    }

    private Calendar getCalendarService(
            OAuth2AuthenticationToken authentication)
            throws IOException, GeneralSecurityException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                "google", authentication.getName());
        if (client == null) {
            throw new IOException("Không tìm thấy thông tin xác thực Google");
        }
        String accessToken = client.getAccessToken().getTokenValue();

        com.google.api.client.auth.oauth2.Credential credential = new com.google.api.client.auth.oauth2.Credential(
                com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod());
        credential.setAccessToken(accessToken);

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @GetMapping("/import-ics-file")
    public String importSpecificIcsFile(
            @RequestParam("fileName") String fileName,
            Model model,
            RedirectAttributes redirectAttributes,
            OAuth2AuthenticationToken authentication) {
        try {
            // Đọc file ICS từ thư mục lưu trữ
            String filePath = Paths.get(uploadProperties.getIcsFolder(), fileName).toString();
            try (InputStream inputStream = new FileInputStream(filePath)) {
                // Parse ICS file
                List<VEvent> events = parseICS(inputStream);
                if (events.isEmpty()) {
                    model.addAttribute("icsError", "File ICS " + fileName + " không chứa sự kiện nào");
                    return "upload";
                }

                // Get Google Calendar service
                Calendar service = getCalendarService(authentication);

                // Insert events
                int successCount = 0;
                for (VEvent event : events) {
                    try {
                        insertEventToGoogle(service, event);
                        successCount++;
                    } catch (Exception e) {
                        System.out.println("Lỗi khi thêm sự kiện: " +
                                (event.getSummary() != null ? event.getSummary().getValue() : "Không có tiêu đề") +
                                " - " + e.getMessage());
                    }
                }

                String message = String.format("Đã thêm thành công %d/%d sự kiện vào Google Calendar",
                        successCount, events.size());
                redirectAttributes.addFlashAttribute("icsMessage", message);
                return "redirect:/api/schedule/upload";
            }
        } catch (Exception e) {
            System.out.println("Lỗi xử lý file ICS " + fileName + ": " + e.getMessage());
            model.addAttribute("icsError", "Lỗi xử lý file ICS " + fileName + ": " + e.getMessage());
            return "upload";
        }
    }
    private void insertEventToGoogle(
            Calendar service,
            VEvent vevent)
            throws IOException {
        Event event = new Event()
                .setSummary(vevent.getSummary() != null ? vevent.getSummary().getValue() : "")
                .setDescription(vevent.getDescription() != null ? vevent.getDescription().getValue() : "");

        if (vevent.getStartDate() != null && vevent.getEndDate() != null) {
            DateTime start = new DateTime(vevent.getStartDate().getDate());
            DateTime end = new DateTime(vevent.getEndDate().getDate());

            event.setStart(new EventDateTime().setDateTime(start));
            event.setEnd(new EventDateTime().setDateTime(end));
        } else {
            System.out.println("Bỏ qua sự kiện không có ngày bắt đầu hoặc kết thúc: " +
                    (vevent.getSummary() != null ? vevent.getSummary().getValue() : "Không có tiêu đề"));
            return;
        }

        try {
            service.events().insert("primary", event).execute();
        } catch (IOException e) {
            System.out.println("Lỗi Google API khi thêm sự kiện: " + e.getMessage());
            throw e;
        }
    }
}