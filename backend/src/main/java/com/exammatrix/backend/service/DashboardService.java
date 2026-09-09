package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getSummary();
}