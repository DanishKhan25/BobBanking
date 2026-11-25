package com.modernbank.dto;

import java.math.BigDecimal;

public class DashboardSummary {
    private long totalCustomers;
    private long totalAccounts;
    private BigDecimal totalBalance;
    private long frozenAccounts;

    public DashboardSummary() {}

    public long getTotalCustomers()    { return totalCustomers; }
    public long getTotalAccounts()     { return totalAccounts; }
    public BigDecimal getTotalBalance(){ return totalBalance; }
    public long getFrozenAccounts()    { return frozenAccounts; }

    public void setTotalCustomers(long totalCustomers)    { this.totalCustomers = totalCustomers; }
    public void setTotalAccounts(long totalAccounts)      { this.totalAccounts = totalAccounts; }
    public void setTotalBalance(BigDecimal totalBalance)  { this.totalBalance = totalBalance; }
    public void setFrozenAccounts(long frozenAccounts)    { this.frozenAccounts = frozenAccounts; }
}
