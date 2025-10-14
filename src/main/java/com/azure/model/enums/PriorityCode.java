package com.azure.model.enums;


public enum PriorityCode {
    LOWEST (1, "가장 낮음", "lowest", 1),
    LOW    (2, "낮음",     "low",     2),
    NORMAL (3, "보통",     "normal",  3),
    HIGH   (4, "높음",     "high",    4),
    HIGHEST(5, "가장 높음","highest", 5);

    public final int id;
    public final String labelKo;   // UI용 한글
    public final String dbName;    // DB name 컬럼
    public final int level;        // DB level 컬럼
    PriorityCode(int id, String labelKo, String dbName, int level){
        this.id = id; this.labelKo = labelKo; this.dbName = dbName; this.level = level;
    }
}