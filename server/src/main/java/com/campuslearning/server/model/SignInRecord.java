package com.campuslearning.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 服务端签到记录实体。
 */
@Entity
@Table(name = "signin_record")
public class SignInRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private Long classId;

    @Column(nullable = false)
    private Long studentId;

    private Double submitLat;
    private Double submitLng;
    private Double distanceMeters;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 255)
    private String resultMessage;

    @Column(nullable = false)
    private LocalDateTime signedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getSubmitLat() {
        return submitLat;
    }

    public void setSubmitLat(Double submitLat) {
        this.submitLat = submitLat;
    }

    public Double getSubmitLng() {
        return submitLng;
    }

    public void setSubmitLng(Double submitLng) {
        this.submitLng = submitLng;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }
}
