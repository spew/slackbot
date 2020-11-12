package org.poker.coronavirus;

public class VirusStats {
    private int totalCases;
    private int totalCasesDelta;
    private int deaths;
    private int deathsDelta;
    private int recoveries;

    private VirusStats(Builder builder) {
        this.totalCases = builder.totalCases;
        this.totalCasesDelta = builder.totalCasesDelta;
        this.deaths = builder.deaths;
        this.deathsDelta = builder.deathsDelta;
        this.recoveries = builder.recoveries;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getTotalCases() {
        return totalCases;
    }

    public int getTotalCasesDelta() {
        return totalCasesDelta;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getDeathsDelta() {
        return deathsDelta;
    }

    public int getRecoveries() {
        return recoveries;
    }

    public static class Builder {
        private int totalCases;
        private int totalCasesDelta;
        private int deaths;
        private int deathsDelta;
        private int recoveries;

        private Builder() {

        }

        public Builder withTotalCases(int totalCases) {
            this.totalCases = totalCases;
            return this;
        }

        public Builder withTotalCasesDelta(int totalCasesDelta) {
            this.totalCasesDelta = totalCasesDelta;
            return this;
        }

        public Builder withDeaths(int deaths) {
            this.deaths = deaths;
            return this;
        }

        public Builder withDeathsDelta(int deathsDelta) {
            this.deathsDelta = deathsDelta;
            return this;
        }

        public Builder withRecoveries(int recoveries) {
            this.recoveries = recoveries;
            return this;
        }

        public VirusStats build() {
            return new VirusStats(this);
        }
    }
}
