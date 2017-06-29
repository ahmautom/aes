package com.maukaming.aes.message;

public abstract class AbstractMessage {
    private String from;
    private String to;

    public AbstractMessage() {}

    protected AbstractMessage(AbstractBuilder builder) {
        this.from = builder.from;
        this.to = builder.to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    protected abstract static class AbstractBuilder<B extends AbstractBuilder> {
        protected String from;
        protected String to;

        public B from(String from) {
            this.from = from;
            return (B) this;
        }

        public B to(String to) {
            this.to = to;
            return (B) this;
        }
    }
}
