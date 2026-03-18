// ========== Billing Types ==========
export type MedicalActCategory = 'CONSULTATION' | 'PROCEDURE' | 'LAB_TEST' | 'IMAGING' | 'TREATMENT' | 'SURGERY' | 'OTHER';
export type InvoiceStatus = 'DRAFT' | 'UNPAID' | 'FINALIZED' | 'PAID' | 'PARTIALLY_PAID' | 'CANCELLED' | 'REFUNDED';
export type PaymentMethodType = 'CASH' | 'CARD' | 'INSURANCE' | 'BANK_TRANSFER' | 'CHECK';

// ========== Medical Act ==========
export interface MedicalAct {
    medicalActId: number;
    code: string;
    name: string;
    description: string;
    category: MedicalActCategory;
    categoryDisplayName: string;
    basePrice: number;
    active: boolean;
    clinicId?: number;
    clinicName?: string;
    departmentId?: number;
    departmentName?: string;
}

export interface MedicalActRequest {
    code: string;
    name: string;
    description?: string;
    category: string;
    basePrice: number;
    clinicId?: number;
    departmentId?: number;
    active?: boolean;
}

// ========== Invoice ==========
export interface Invoice {
    invoiceId: number;
    invoiceNumber: string;
    createdAt: string;
    updatedAt?: string;
    status: InvoiceStatus;
    totalAmount: number;
    paidAmount: number;
    remainingAmount: number;
    discountAmount?: number;
    discountReason?: string;
    patientId: number;
    patientName: string;
    appointmentId?: number;
    appointmentDate?: string;
    createdById?: number;
    createdByName?: string;
    clinicId?: number;
    clinicName?: string;
    lines: InvoiceLine[];
    payments: PaymentRecord[];
    notes?: string;
}

export interface InvoiceRequest {
    patientId: number;
    appointmentId?: number;
    clinicId?: number;
    lines?: InvoiceLineRequest[];
    discountAmount?: number;
    discountReason?: string;
    notes?: string;
}

export interface InvoiceLine {
    lineId: number;
    medicalActId: number;
    medicalActCode: string;
    medicalActName: string;
    description: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
}

export interface InvoiceLineRequest {
    medicalActId: number;
    quantity?: number;
    unitPrice?: number;
}

// ========== Payment ==========
export interface PaymentRecord {
    paymentId: number;
    invoiceId: number;
    invoiceNumber: string;
    amount: number;
    paymentMethod: PaymentMethodType;
    paymentDate: string;
    reference?: string;
    receivedById?: number;
    receivedByName?: string;
    patientName?: string;
    notes?: string;
}

export interface PaymentRequest {
    invoiceId: number;
    amount: number;
    paymentMethod: string;
    reference?: string;
    notes?: string;
}

// ========== Performed Act (doctor adds during consultation) ==========
export interface PerformedAct {
    performedActId: number;
    appointmentId: number;
    medicalActId: number;
    medicalActCode: string;
    medicalActName: string;
    category: string;
    performedById: number;
    performedByName: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
    notes?: string;
    performedAt: string;
}

export interface PerformedActRequest {
    medicalActId: number;
    quantity?: number;
    notes?: string;
}

// ========== Price Estimate (before booking) ==========
export interface PriceEstimate {
    baseConsultationPrice: number;
    estimatedActs: EstimatedAct[];
    totalEstimatedPrice: number;
    practitionerName: string;
    specialtyName?: string;
}

export interface EstimatedAct {
    actCode: string;
    actName: string;
    category: string;
    price: number;
}

// ========== Time Slot (scheduling grid) ==========
export interface TimeSlot {
    startTime: string;
    endTime: string;
    datetime: string;
    available: boolean;
    appointmentId?: number;
    patientName?: string;
    status?: string;
}

// ========== Daily Summary ==========
export interface DailySummary {
    date: string;
    totalRevenue: number;
    totalCash: number;
    totalCard: number;
    totalInsurance: number;
    totalBankTransfer: number;
    totalCheck: number;
    invoiceCount: number;
    paidCount: number;
    pendingCount: number;
    cancelledCount: number;
    topMedicalActs: { name: string; code: string; count: number; revenue: number }[];
}
