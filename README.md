# WelHome_Kotlin-21

## Book Visit Feature

The Book Visit feature allows users to schedule property visits by selecting available dates and time slots. This feature provides a seamless experience for students to arrange viewing appointments for housing properties.

### Feature Overview

The Book Visit feature enables users to:
- View available dates for property visits
- Select specific time slots on chosen dates
- Confirm booking appointments
- Track their scheduled visits

### User Flow

1. **Initiation**: User navigates to a property detail page and clicks the "Book Visit" button
2. **Date Selection**: User is presented with a date picker to choose their preferred visit date
3. **Time Slot Selection**: After selecting a date, available time slots are displayed in a grid layout
4. **Confirmation**: User confirms their selected date and time slot
5. **Booking Created**: The system creates a booking record in Firebase

### Technical Architecture

#### Components

**UI Layer:**
- `BookVisitActivity`: Android Activity that hosts the Book Visit screen
    - Receives `housingId` as an intent extra to identify the property
    - Uses Jetpack Compose for UI rendering

- `BookVisitView`: Composable UI component that displays:
    - Back navigation button
    - Material3 DatePicker for date selection
    - Grid of available time slots (2 columns)
    - Confirmation button (enabled only when both date and time are selected)

- `BookVisitViewModel`: Manages UI state and business logic
    - Loads availability data from repository
    - Handles date and time selection
    - Maintains booking state

- `BookVisitUiState`: Data class representing the UI state
    - `isLoading`: Loading state indicator
    - `error`: Error message if any
    - `housingId`: Property identifier
    - `selectedDateMillis`: Selected date in milliseconds (UTC)
    - `selectedHour`: Selected time slot as string
    - `availableHours`: List of available time slots for selected date
    - `availabilityByDay`: Map of dates to available time slots

**Data Layer:**
- `BookingScheduleRepository`: Handles fetching availability data
    - `getAvailabilityByDay()`: Retrieves available dates and time slots from Firebase
    - Supports multiple housing ID formats (String, DocumentReference, path)
    - Filters slots based on available capacity
    - Returns LocalDate to LocalTime mappings

- `BookingRepository`: Manages booking records
    - `getUserBookings()`: Fetches user's existing bookings
    - Integrates with Firebase Authentication

**Models:**
- `Booking`: Represents a confirmed visit booking
    - `id`: Unique booking identifier
    - `housing`: Housing property reference
    - `housingTitle`: Property name
    - `thumbnail`: Property image URL
    - `state`: Booking status (Scheduled, Completed, Missed)
    - `date`: Visit date
    - `slot`: Time slot
    - `confirmedVisit`: Confirmation flag
    - `user`: User ID
    - `userComment`: User feedback
    - `ownerComment`: Owner notes
    - `rating`: User rating

- `BookingSchedule`: Represents property availability schedule
    - `id`: Schedule identifier
    - `availableDates`: Number of available dates
    - `updatedAt`: Last update timestamp
    - `housing`: Housing property reference
    - `bookingDate`: List of BookingDate objects

- `BookingDate`: Represents availability for a specific date
    - `id`: Date record identifier
    - `date`: The date
    - `availableSlots`: Number of slots available
    - `bookingSlot`: List of BookingSlot objects

- `BookingSlot`: Represents a specific time slot
    - `id`: Slot identifier
    - `time`: Time of the slot
    - `duration`: Duration in hours
    - `availableUsers`: Capacity available
    - `description`: Optional description

#### Data Flow

1. User taps "Book Visit" on property detail page
2. `DetailHousingRoute` launches `BookVisitActivity` with housing ID
3. `BookVisitViewModel.load()` is called with housing ID
4. Repository queries Firebase `BookingSchedule` collection
5. Available dates and time slots are filtered by capacity
6. UI displays date picker with default selection (today)
7. When user selects a date, available time slots are updated
8. User selects a time slot (marked with checkmark)
9. User taps "Confirm date" button
10. Booking is ready to be saved (implementation pending)

#### Firebase Collections

**BookingSchedule Collection:**
- Document per property
- Field `housing`: Reference to HousingPost
- Subcollection `BookingDate`: Contains dates with availability
    - Field `date`: Timestamp for the date
    - Subcollection `BookingSlot`: Contains time slots
        - Field `time`: Timestamp for the slot
        - Field `availableUsers`: Number of available spots
        - Field `duration`: Duration in hours
        - Field `description`: Optional description

**Booking Collection:**
- Document per confirmed booking
- Fields:
    - `user`: User ID who made the booking
    - `housing`: Housing property reference
    - `housingTitle`: Property name
    - `date`: Visit date
    - `slot`: Time slot
    - `state`: Booking status
    - `confirmedVisit`: Confirmation flag
    - `rating`: User rating
    - `userComment`, `ownerComment`: Feedback comments

### Key Features

**Smart Availability Detection:**
- Handles multiple housing ID reference formats
- Filters out fully booked time slots
- Groups availability by day for easy navigation

**User-Friendly Interface:**
- Material3 DatePicker for intuitive date selection
- Grid layout for time slots (2 columns)
- Visual feedback with checkmark for selected time
- Disabled confirmation button until both date and time are selected
- Shows "No availability" message when date has no slots

**Time Zone Handling:**
- Uses UTC for date grouping (consistent with DatePicker)
- Uses system time zone for displaying time slots to users
- Properly converts between milliseconds and LocalDate/LocalTime

### Current Limitations

- Confirmation action is not yet connected to Firebase (placeholder in ViewModel)
- No conflict detection for overlapping bookings
- No email/notification system for booking confirmations
- Capacity management is read-only (no decrement on booking)

### Future Enhancements

- Implement booking confirmation with Firebase persistence
- Add booking conflict detection
- Send confirmation notifications
- Implement capacity management
- Add booking cancellation functionality
- Support recurring availability schedules