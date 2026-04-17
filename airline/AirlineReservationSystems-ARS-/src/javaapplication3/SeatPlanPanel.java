package javaapplication3;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Grid of JLabels representing seats. 1..seatCount.
 */
public class SeatPlanPanel extends JPanel {
    private final int seatCount;
    private final List<JLabel> seatButtons = new ArrayList<>();
    Border border = BorderFactory.createLineBorder(Color.WHITE, 2);
    Border redBorder = BorderFactory.createLineBorder(Color.RED, 2);

    public SeatPlanPanel(int seatCount) {
        this.seatCount = seatCount;
        this.setLayout(new GridLayout(4, 6, 10, 10));
        for (int i = 0; i < this.seatCount; i++) {
            String seatNo = Integer.toString(i + 1);
            JLabel seat = new JLabel(seatNo);
            seat.setHorizontalAlignment(JLabel.CENTER);
            seat.setVerticalAlignment(JLabel.CENTER);
            seat.setFont(new Font("Serif", Font.BOLD, 22));
            seat.setOpaque(true);                // ensure background is painted
            seat.setBackground(Color.GREEN);     // default available color
            seat.setForeground(Color.WHITE);
            seat.setBorder(border);
            seatButtons.add(seat);
            this.add(seat);
        }
    }

    public void bookSeat(int seatNo) {
        if (seatNo < 1 || seatNo > seatCount) return;
        JLabel lbl = seatButtons.get(seatNo - 1);
        lbl.setOpaque(true);
        lbl.setBorder(redBorder);
        lbl.setBackground(Color.RED);
        lbl.setForeground(Color.WHITE);
    }

    public void availableSeats(HashMap<Integer, Long> seatsMap) {
        if (seatsMap == null) return;
        for (int i = 1; i <= seatCount; i++) {
            Long val = seatsMap.get(i);
            JLabel lbl = seatButtons.get(i - 1);
            if (val == null || val == 0L) {
                lbl.setOpaque(true);
                lbl.setBackground(Color.GREEN);
                lbl.setForeground(Color.WHITE);
                lbl.setBorder(border);
            } else {
                bookSeat(i);
            }
        }
    }

    public void cancelSeat(int seatNo) {
        if (seatNo < 1 || seatNo > seatCount) return;
        JLabel lbl = seatButtons.get(seatNo - 1);
        lbl.setBorder(border);
        lbl.setBackground(Color.GREEN);
        lbl.setForeground(Color.WHITE);
    }
}
