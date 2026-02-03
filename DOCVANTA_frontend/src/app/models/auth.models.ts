// ========== Type Definitions ==========
export type RoleType = 'PRACTITIONER' | 'PATIENT' | 'SYSTEM_ADMINISTRATOR' | 'RECEPTIONIST';

// ========== Authentication ==========
export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    role: RoleType;
    // Patient-specific
    dob?: string;
    address?: string;
    // Practitioner-specific
    specialties?: string;
    specialtyIds?: number[];
}

export interface AuthResponse {
    token: string;
    username: string;
    role: string;
    userId: number;
}

export interface User {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    role: RoleType;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
    status: number;
}

// ========== Practitioner (formerly Doctor) ==========
export interface Practitioner {
    userId: number;
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    enabled: boolean;
    specialties: string[];
    certifications: Certification[];
    competencies: Competency[];
    schedules: PractitionerSchedule[];
    departmentId?: number;
    departmentName?: string;
    clinicId?: number;
    clinicName?: string;
    clinicalAssistantIds?: number[];
}

// ========== Clinic Personnel (formerly Staff) ==========
export interface ClinicPersonnel {
    userId: number;
    username: string;
    firstName: string;
    lastName: string;
    personnelType: string;
    enabled: boolean;
    clinicId?: number;
    clinicName?: string;
    supervisingPractitionerIds?: number[];
    departmentIds?: number[];
    departmentNames?: string[];
}

// ========== Patient ==========
export interface Patient {
    userId: number;
    username: string;
    firstName: string;
    lastName: string;
    dob: string;
    phone: string;
    email: string;
    address: string;
    enabled: boolean;
    clinicId?: number;
    clinicName?: string;
    patientRecordId?: number;
}

// ========== Appointments ==========
export type AppointmentStatusType = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'INVOICED' | 'PAID' | 'REJECTED' | 'CANCELLED';
export type PaymentStatusType = 'UNPAID' | 'PARTIALLY_PAID' | 'PAID';

export interface Appointment {
    appointmentId: number;
    referenceNumber: string;
    datetime: string;
    status: AppointmentStatusType;
    paymentStatus: PaymentStatusType;
    practitionerId: number;
    practitionerName: string;
    patientId: number;
    patientName: string;
    specialtyId?: number;
    specialtyName?: string;
    reason?: string;
    estimatedPrice?: number;
    finalPrice?: number;
    invoiceId?: number;
    invoiceStatus?: string;
    performedActs?: PerformedActSummary[];
}

export interface PerformedActSummary {
    performedActId: number;
    medicalActName: string;
    quantity: number;
    totalPrice: number;
}

export interface AppointmentRequest {
    datetime: string;
    practitionerId: number;
    patientId: number;
    status?: string;
    reason?: string;
}

export interface AppointmentBySpecialtyRequest {
    patientId: number;
    specialtyId: number;
    practitionerId?: number;
    preferredDatetime: string;
    reason?: string;
}

// ========== Medical Documents ==========
export interface MedicalDocument {
    documentId: number;
    type: string;
    content: string;
    createdDate: string;
    authorizedForPatient: boolean;
    patientId: number;
    patientName: string;
    practitionerId: number;
    practitionerName: string;
    templateId?: number;
}

export interface MedicalDocumentRequest {
    type: string;
    content: string;
    patientId: number;
    practitionerId: number;
    templateId?: number;
    authorizedForPatient?: boolean;
}

// ========== Patient Record (formerly MedicalRecord) ==========
export interface PatientRecord {
    recordId: number;
    bloodType: string;
    allergies: string;
    chronicDiseases: string;
    notes: string;
    patientId: number;
    patientName?: string;
}

// ========== Clinic ==========
export interface Clinic {
    clinicId: number;
    name: string;
    address: string;
}

// ========== Clinical Department (formerly Department) ==========
export interface ClinicalDepartment {
    departmentId: number;
    name: string;
    clinicId: number;
    clinicName?: string;
    practitionerCount?: number;
}

// ========== Specialty ==========
export interface Specialty {
    specialtyId: number;
    name: string;
}

// ========== Certification ==========
export interface Certification {
    certId: number;
    name: string;
    issueDate: string;
    expiryDate: string;
}

// ========== Competency ==========
export interface Competency {
    compId: number;
    name: string;
    description: string;
}

// ========== Practitioner Schedule (formerly Schedule) ==========
export interface PractitionerSchedule {
    scheduleId: number;
    dayOfWeek: string;
    startTime: string;
    endTime: string;
}

