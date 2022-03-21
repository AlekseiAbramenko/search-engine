package ru.skillbox;

public class Elevator {

    private int currentFloor = 1;
    private int minFloor;
    private int maxFloor;

    public Elevator(int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void moveDown() {
        currentFloor = currentFloor > minFloor ? currentFloor - 1 : currentFloor;
    }

    public void moveUp() {
        currentFloor = currentFloor < maxFloor ? currentFloor + 1 : currentFloor;
    }

    public void move(int floor) {
        if(floor < minFloor || floor > maxFloor) {
            System.out.println("Error");
            return;
        }

        if(floor == getCurrentFloor()) {
            return;
        }

        if(getCurrentFloor() < floor) {
            while(getCurrentFloor() != floor) {
                moveUp();
                System.out.println(getCurrentFloor());
            }
        }

        if(floor < getCurrentFloor()) {
            while(getCurrentFloor() != floor) {
                moveDown();
                System.out.println(getCurrentFloor());
            }
        }
    }

}