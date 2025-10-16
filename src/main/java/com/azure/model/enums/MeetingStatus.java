// /model/meeting/MeetingStatus.java
package com.azure.model.enums;

public enum MeetingStatus {
    SCHEDULED,  // 예정됨
    RECORDING,  // 녹음 중
    TRANSCRIBING, // 텍스트 변환 중
    COMPLETED,  // 변환 완료
    FAILED      // 실패
}