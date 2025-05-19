package com.example.demo_tttn.services;

import com.example.demo_tttn.models.GiangVien;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class GiangVienService {
    public static GiangVien processGiangVien(String cellValue) {
        String tenGiangVien = "";
        String maGiangVien = "";
        String[] separators = {"Cán bộ giảng dạy: ", "CBGD ", " \\("};
        String[] result = cellValue.split(String.join("|", separators));
        List<String> lstr = Arrays.stream(result)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (lstr.size() < 2) {
            tenGiangVien = cellValue.trim();
        } else {
            tenGiangVien = lstr.get(0).trim();
            maGiangVien = lstr.get(1).replace(")", "").trim();
        }

        return new GiangVien(maGiangVien, tenGiangVien, "sdt", "khoa");
    }


}
