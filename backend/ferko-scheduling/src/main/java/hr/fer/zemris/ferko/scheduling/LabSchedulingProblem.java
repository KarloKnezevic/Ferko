package hr.fer.zemris.ferko.scheduling;

/**
 * Laboratory exercise timetabling (Čupić's thesis 4.4). Each of {@code N} lab events is assigned to
 * one of {@code T} time-slots; gene {@code i} = slot index for event {@code i} and {@code
 * optionCount} is {@code T} for every gene.
 *
 * <p>A slot offers a total student capacity (sum of capacities of the rooms available in that slot)
 * and a fixed quantity of each limited shared resource (e.g. licences). Two soft, non-linear
 * penalties are accumulated, each squared per Čupić's quadratic over-use weighting:
 *
 * <ul>
 *   <li><b>capacityOverflow</b> — per slot, {@code max(0, Σ eventStudents − slotRoomCapacity)^2}.
 *   <li><b>resourceOveruse</b> — per slot and per resource, {@code max(0, simultaneousUses −
 *       resourceQuantity)^2}, where an event with {@code eventResource[i] == -1} needs no resource.
 * </ul>
 *
 * <p>A penalty of 0 means every slot fits its events into the available room capacity and never
 * over-subscribes a limited resource — i.e. all hard constraints are satisfied.
 */
public final class LabSchedulingProblem implements Problem {

  private final int timeSlots;
  private final int[] eventStudents;
  private final int[] slotRoomCapacity;
  private final int[] eventResource;
  private final int[] resourceQuantity;

  /**
   * Creates a laboratory timetabling problem.
   *
   * @param timeSlots number of available time-slots ({@code T}), must be {@code >= 1}
   * @param eventStudents students needing each event, one entry per event, each {@code >= 0}
   * @param slotRoomCapacity total student capacity per slot, length {@code timeSlots}, each {@code
   *     >= 0}
   * @param eventResource limited resource required by each event, or {@code -1} for none; length
   *     equals {@code eventStudents.length} and valid indices are {@code [0,
   *     resourceQuantity.length)}
   * @param resourceQuantity max simultaneous uses per resource, each {@code >= 0}
   * @throws IllegalArgumentException if any argument is null or inconsistent
   */
  public LabSchedulingProblem(
      int timeSlots,
      int[] eventStudents,
      int[] slotRoomCapacity,
      int[] eventResource,
      int[] resourceQuantity) {
    if (timeSlots < 1) {
      throw new IllegalArgumentException("timeSlots must be >= 1");
    }
    if (eventStudents == null) {
      throw new IllegalArgumentException("eventStudents must not be null");
    }
    if (slotRoomCapacity == null || slotRoomCapacity.length != timeSlots) {
      throw new IllegalArgumentException("slotRoomCapacity must have length timeSlots");
    }
    if (eventResource == null || eventResource.length != eventStudents.length) {
      throw new IllegalArgumentException("eventResource must have length eventStudents.length");
    }
    if (resourceQuantity == null) {
      throw new IllegalArgumentException("resourceQuantity must not be null");
    }
    for (int students : eventStudents) {
      if (students < 0) {
        throw new IllegalArgumentException("eventStudents must be >= 0");
      }
    }
    for (int capacity : slotRoomCapacity) {
      if (capacity < 0) {
        throw new IllegalArgumentException("slotRoomCapacity must be >= 0");
      }
    }
    for (int quantity : resourceQuantity) {
      if (quantity < 0) {
        throw new IllegalArgumentException("resourceQuantity must be >= 0");
      }
    }
    for (int resource : eventResource) {
      if (resource < -1 || resource >= resourceQuantity.length) {
        throw new IllegalArgumentException("eventResource out of range");
      }
    }
    this.timeSlots = timeSlots;
    this.eventStudents = eventStudents.clone();
    this.slotRoomCapacity = slotRoomCapacity.clone();
    this.eventResource = eventResource.clone();
    this.resourceQuantity = resourceQuantity.clone();
  }

  /** Returns the number of lab events to schedule. */
  public int eventCount() {
    return eventStudents.length;
  }

  /** Returns the number of available time-slots. */
  public int timeSlotCount() {
    return timeSlots;
  }

  /** Returns the number of distinct limited resources. */
  public int resourceCount() {
    return resourceQuantity.length;
  }

  @Override
  public int geneCount() {
    return eventStudents.length;
  }

  @Override
  public int optionCount(int gene) {
    return timeSlots;
  }

  @Override
  public double penalty(int[] genes) {
    if (genes == null || genes.length != eventStudents.length) {
      throw new IllegalArgumentException("genes must have length eventCount()");
    }
    int[] slotLoad = new int[timeSlots];
    int[][] slotResourceUse = new int[timeSlots][resourceQuantity.length];
    for (int event = 0; event < genes.length; event++) {
      int slot = genes[event];
      if (slot < 0 || slot >= timeSlots) {
        throw new IllegalArgumentException("gene out of range: " + slot);
      }
      slotLoad[slot] += eventStudents[event];
      int resource = eventResource[event];
      if (resource >= 0) {
        slotResourceUse[slot][resource]++;
      }
    }

    double penalty = 0.0;
    for (int slot = 0; slot < timeSlots; slot++) {
      int capacityOverflow = slotLoad[slot] - slotRoomCapacity[slot];
      if (capacityOverflow > 0) {
        penalty += Math.pow(capacityOverflow, 2);
      }
      for (int resource = 0; resource < resourceQuantity.length; resource++) {
        int resourceOveruse = slotResourceUse[slot][resource] - resourceQuantity[resource];
        if (resourceOveruse > 0) {
          penalty += Math.pow(resourceOveruse, 2);
        }
      }
    }
    return penalty;
  }
}
