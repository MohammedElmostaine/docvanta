import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-about',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './about.component.html',
    styleUrls: ['./about.component.css']
})
export class AboutComponent {
    values = [
        { icon: 'shield', title: 'Security First', desc: 'Every feature is built with medical data privacy at its core. Role-based access ensures only authorized personnel see sensitive information.' },
        { icon: 'users', title: 'User-Centered Design', desc: 'Four distinct portals tailored for patients, practitioners, receptionists, and administrators — each optimized for their daily workflow.' },
        { icon: 'zap', title: 'Operational Efficiency', desc: 'From appointment booking to payment collection, every process is streamlined to eliminate manual overhead and reduce errors.' },
        { icon: 'layers', title: 'Integrated Platform', desc: 'No more switching between systems. Scheduling, records, billing, and administration all work together seamlessly.' }
    ];

    techStack = [
        { name: 'Spring Boot', role: 'Backend API & Security', icon: 'server' },
        { name: 'Angular', role: 'Frontend Application', icon: 'layout' },
        { name: 'PostgreSQL', role: 'Relational Database', icon: 'database' },
        { name: 'JWT', role: 'Authentication & Authorization', icon: 'key' }
    ];
}
