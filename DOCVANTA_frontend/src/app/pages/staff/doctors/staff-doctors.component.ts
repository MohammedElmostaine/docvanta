import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PractitionerService } from '../../../services/practitioner.service';
import { AppointmentService } from '../../../services/appointment.service';
import { Practitioner, Appointment, PractitionerSchedule } from '../../../models/auth.models';

@Component({
    selector: 'app-staff-doctors',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './staff-doctors.component.html',
    styleUrls: ['./staff-doctors.component.css']
})
export class StaffDoctorsComponent implements OnInit {
    doctors: Practitioner[] = [];
    filteredDoctors: Practitioner[] = [];
    todayAppointments: Appointment[] = [];
    loading = true;
    searchQuery = '';
    errorMessage = '';

    // Doctor detail modal
    showDetailModal = false;
    selectedDoctor: Practitioner | null = null;
    doctorAppointments: Appointment[] = [];
    loadingDoctorDetails = false;

    // Days of week for schedule display
    daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

    private allScopedAppointments: Appointment[] = [];

    constructor(
        private practitionerService: PractitionerService,
        private appointmentService: AppointmentService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        let completed = 0;
        const checkDone = () => {
            completed++;
            if (completed >= 2) {
                this.loading = false;
                this.cdr.detectChanges();
            }
        };

        this.practitionerService.getAll().subscribe({
            next: (data) => {
                this.doctors = data || [];
                this.filteredDoctors = [...this.doctors];
                checkDone();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to load doctors';
                checkDone();
            }
        });

        this.appointmentService.getAll().subscribe({
            next: (appointments: Appointment[]) => {
                this.allScopedAppointments = appointments || [];
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);

                this.todayAppointments = this.allScopedAppointments.filter((apt: Appointment) => {
                    const aptDate = new Date(apt.datetime);
                    return aptDate >= today && aptDate < tomorrow;
                });
                checkDone();
            },
            error: () => checkDone()
        });
    }

    onSearch(): void {
        const q = this.searchQuery.toLowerCase().trim();
        if (!q) {
            this.filteredDoctors = [...this.doctors];
            return;
        }
        this.filteredDoctors = this.doctors.filter(d =>
            (d.firstName + ' ' + d.lastName).toLowerCase().includes(q) ||
            d.email?.toLowerCase().includes(q) ||
            this.getSpecialties(d).toLowerCase().includes(q)
        );
    }

    getSpecialties(doctor: Practitioner): string {
        return doctor.specialties?.join(', ') || 'General';
    }

    getDoctorTodayAppointmentsCount(doctorId: number): number {
        return this.todayAppointments.filter(apt => apt.practitionerId === doctorId).length;
    }

    getDoctorNextAppointment(doctorId: number): Appointment | undefined {
        const now = new Date();
        return this.todayAppointments
            .filter(apt => apt.practitionerId === doctorId && new Date(apt.datetime) >= now)
            .sort((a, b) => new Date(a.datetime).getTime() - new Date(b.datetime).getTime())[0];
    }

    viewDoctorDetails(doctor: Practitioner): void {
        this.selectedDoctor = doctor;
        this.showDetailModal = true;
        this.loadingDoctorDetails = true;

        // Use already-loaded scoped appointments instead of fetching all
        const now = new Date();
        this.doctorAppointments = this.allScopedAppointments
            .filter((apt: Appointment) => apt.practitionerId === doctor.userId && new Date(apt.datetime) >= now)
            .sort((a: Appointment, b: Appointment) =>
                new Date(a.datetime).getTime() - new Date(b.datetime).getTime()
            )
            .slice(0, 10);
        this.loadingDoctorDetails = false;
    }

    closeDetailModal(): void {
        this.showDetailModal = false;
        this.selectedDoctor = null;
        this.doctorAppointments = [];
    }

    getScheduleForDay(doctor: Practitioner, day: string): PractitionerSchedule | undefined {
        return doctor.schedules?.find(s => s.dayOfWeek?.toUpperCase() === day);
    }

    formatScheduleTime(time: string): string {
        if (!time) return '';
        // Handle HH:mm:ss format
        const parts = time.split(':');
        if (parts.length >= 2) {
            const hour = parseInt(parts[0], 10);
            const minute = parts[1];
            const ampm = hour >= 12 ? 'PM' : 'AM';
            const displayHour = hour % 12 || 12;
            return `${displayHour}:${minute} ${ampm}`;
        }
        return time;
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    getStatusClass(status: string): string {
        switch (status?.toUpperCase()) {
            case 'PENDING': return 'status-pending';
            case 'CONFIRMED': return 'status-confirmed';
            case 'CANCELLED': return 'status-cancelled';
            case 'COMPLETED': return 'status-completed';
            default: return 'status-pending';
        }
    }

    getDayShort(day: string): string {
        const shorts: Record<string, string> = {
            'MONDAY': 'Mon',
            'TUESDAY': 'Tue',
            'WEDNESDAY': 'Wed',
            'THURSDAY': 'Thu',
            'FRIDAY': 'Fri',
            'SATURDAY': 'Sat',
            'SUNDAY': 'Sun'
        };
        return shorts[day] || day;
    }

    isToday(day: string): boolean {
        const today = new Date().toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase();
        return day === today;
    }
}
