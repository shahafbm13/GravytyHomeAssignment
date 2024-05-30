public class Connection {
    public String name;
    public String job;
    public String connectionTime;

    public Connection(String name, String job, String connectionTime) {
        this.name = name;
        this.job = job;
        this.connectionTime = connectionTime;
    }

    public String getName() {
        return name;
    }

    public String setName(String name) {
        return this.name = name;
    }

    public String getJob() {
        return job;
    }

    public String setJob(String job) {
        return this.job = job;
    }

    public String getConnectionTime() {
        return connectionTime;
    }

    public String setConnectionTime(String connectionTime) {
        return this.connectionTime= connectionTime;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", occupation='" + job + '\'' +
                ", location='" +  + '\'' +
                '}';
    }
}
