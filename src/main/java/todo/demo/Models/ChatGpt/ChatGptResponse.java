package todo.demo.Models.ChatGpt;

public  class ChatGptResponse {
    private Choice[] choices;

    public Choice[] getChoices() {
        return choices;
    }

    public void setChoices(Choice[] choices) {
        this.choices = choices;
    }

    public static class Choice {
        private MessageGpt message;

        public MessageGpt getMessage() {
            return message;
        }

        public void setMessage(MessageGpt message) {
            this.message = message;
        }
    }
}
