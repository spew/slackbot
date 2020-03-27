package org.poker.coronavirus;

public class VirusStats {
    private int total;
    private int deaths;
    private int recoveries;

    private VirusStats(Builder builder) {
        this.total = builder.total;
        this.deaths = builder.deaths;
        this.recoveries = builder.recoveries;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getTotal() {
        return total;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getRecoveries() {
        return recoveries;
    }

    public static class Builder {
        private int total;
        private int deaths;
        private int recoveries;

        private Builder() {

        }

        public Builder withTotal(int total) {
            this.total = total;
            return this;
        }

        public Builder withDeaths(int deaths) {
            this.deaths = deaths;
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
