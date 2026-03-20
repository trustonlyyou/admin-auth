package com.levely.auth.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class WalletPointIssueHist {
    @Id
    private String seq;

    private String tradeType;
}
