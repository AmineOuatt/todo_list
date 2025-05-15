package Model;

import java.util.Date;

public class RecurringPattern {
    private int patternId;
    private String patternType;  // daily, weekly, monthly, yearly, custom
    private int intervalValue;
    private Integer dayOfWeek;   // For weekly patterns
    private Integer dayOfMonth;  // For monthly patterns
    private Integer monthOfYear; // For yearly patterns
    private Date endDate;
    private Integer maxOccurrences;
    
    public RecurringPattern() {
    }
    
    public RecurringPattern(int patternId, String patternType, int intervalValue) {
        this.patternId = patternId;
        this.patternType = patternType;
        this.intervalValue = intervalValue;
    }
    
    public RecurringPattern(int patternId, String patternType, int intervalValue, Date endDate) {
        this.patternId = patternId;
        this.patternType = patternType;
        this.intervalValue = intervalValue;
        this.endDate = endDate;
    }
    
    public RecurringPattern(int patternId, String patternType, int intervalValue, 
                           Integer dayOfWeek, Integer dayOfMonth, Integer monthOfYear,
                           Date endDate, Integer maxOccurrences) {
        this.patternId = patternId;
        this.patternType = patternType;
        this.intervalValue = intervalValue;
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        this.endDate = endDate;
        this.maxOccurrences = maxOccurrences;
    }
    
    public int getPatternId() {
        return patternId;
    }
    
    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }
    
    public String getPatternType() {
        return patternType;
    }
    
    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }
    
    public int getIntervalValue() {
        return intervalValue;
    }
    
    public void setIntervalValue(int intervalValue) {
        this.intervalValue = intervalValue;
    }
    
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public Integer getDayOfMonth() {
        return dayOfMonth;
    }
    
    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }
    
    public Integer getMonthOfYear() {
        return monthOfYear;
    }
    
    public void setMonthOfYear(Integer monthOfYear) {
        this.monthOfYear = monthOfYear;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public Integer getMaxOccurrences() {
        return maxOccurrences;
    }
    
    public void setMaxOccurrences(Integer maxOccurrences) {
        this.maxOccurrences = maxOccurrences;
    }
    
    @Override
    public String toString() {
        String base = patternType + " (every " + intervalValue;
        
        switch (patternType.toLowerCase()) {
            case "daily":
                base += " day(s))";
                break;
            case "weekly":
                base += " week(s))";
                break;
            case "monthly":
                base += " month(s))";
                break;
            case "yearly":
                base += " year(s))";
                break;
            default:
                base += ")";
        }
        
        if (endDate != null) {
            base += " until " + endDate;
        }
        
        return base;
    }
} 