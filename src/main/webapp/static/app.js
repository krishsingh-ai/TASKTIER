/**
 * TaskTier — Plain JavaScript Client Interactions
 * (No frameworks, pure Vanilla JS)
 */

// Immediately apply saved theme before DOMContentLoaded to prevent theme flash
(function() {
    const savedTheme = localStorage.getItem('tasktier-theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
})();

document.addEventListener('DOMContentLoaded', () => {
    initThemeToggle();
    initMobileNav();
    initCategoryModal();
    initTimer();
    initFilters();
    initConfirmations();
    initCountUp();
    initTaskCreateForm();
    initFormValidation();
    initEmptyStateActions();
    initToasts();
});

/**
 * Theme Toggle (Light / Dark Mode persisted in localStorage)
 */
function initThemeToggle() {
    const toggleBtns = document.querySelectorAll('#theme-toggle, .theme-toggle-btn');
    if (!toggleBtns || toggleBtns.length === 0) return;

    function updateIcons(currentTheme) {
        toggleBtns.forEach(btn => {
            const sunIcon = btn.querySelector('.sun-icon');
            const moonIcon = btn.querySelector('.moon-icon');
            if (sunIcon && moonIcon) {
                if (currentTheme === 'dark') {
                    sunIcon.style.display = 'none';
                    moonIcon.style.display = 'inline-block';
                } else {
                    sunIcon.style.display = 'inline-block';
                    moonIcon.style.display = 'none';
                }
            }
        });
    }

    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    updateIcons(currentTheme);

    toggleBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const activeTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const newTheme = activeTheme === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('tasktier-theme', newTheme);
            updateIcons(newTheme);
        });
    });
}

/**
 * Category Creation Modal Interactions
 */
function initCategoryModal() {
    const openBtn = document.getElementById('openCategoryModal');
    const modal = document.getElementById('categoryModal');
    const form = document.getElementById('categoryModalForm');
    const cancelBtn = document.getElementById('cancelCategoryModal');
    const closeBtn = document.getElementById('closeCategoryModalBtn');
    const errorEl = document.getElementById('categoryError');
    const nameInput = document.getElementById('modalCategoryName');
    const descInput = document.getElementById('modalCategoryDescription');
    const colorInput = document.getElementById('categoryColor');

    if (!modal || !openBtn) return;

    function openModal() {
        if (form) form.reset();
        if (colorInput) colorInput.value = '#8b5cf6';
        if (errorEl) {
            errorEl.style.display = 'none';
            errorEl.textContent = '';
        }
        modal.style.display = 'flex';
        modal.setAttribute('aria-hidden', 'false');
        if (nameInput) nameInput.focus();
    }

    function closeModal() {
        modal.style.display = 'none';
        modal.setAttribute('aria-hidden', 'true');
        if (errorEl) {
            errorEl.style.display = 'none';
            errorEl.textContent = '';
        }
    }

    openBtn.addEventListener('click', openModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);
    if (closeBtn) closeBtn.addEventListener('click', closeModal);

    // Close on clicking backdrop
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });

    // Close on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modal.style.display === 'flex') {
            closeModal();
        }
    });

    // Handle AJAX Form Submit
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (errorEl) {
                errorEl.style.display = 'none';
                errorEl.textContent = '';
            }

            const name = nameInput ? nameInput.value.trim() : '';
            const description = descInput ? descInput.value.trim() : '';
            const color = colorInput ? colorInput.value : '#8b5cf6';

            if (!name) {
                if (errorEl) {
                    errorEl.textContent = 'Category name is required.';
                    errorEl.style.display = 'block';
                }
                if (nameInput) nameInput.focus();
                return;
            }

            if (name.length > 50) {
                if (errorEl) {
                    errorEl.textContent = 'Category name cannot exceed 50 characters.';
                    errorEl.style.display = 'block';
                }
                if (nameInput) nameInput.focus();
                return;
            }

            const contextPath = window.location.pathname.startsWith('/tasktier') ? '/tasktier' : '';

            try {
                const response = await fetch(contextPath + '/categories', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({
                        name: name,
                        description: description,
                        color: color
                    })
                });

                const data = await response.json().catch(() => ({}));

                if (!response.ok) {
                    const errorMsg = data.error || (data.fieldErrors && Object.values(data.fieldErrors).join(', ')) || 'Failed to create category.';
                    if (errorEl) {
                        errorEl.textContent = errorMsg;
                        errorEl.style.display = 'block';
                    }
                    return;
                }

                // Append new category to dropdown and auto-select
                const categorySelect = document.getElementById('categoryId');
                if (categorySelect) {
                    const option = document.createElement('option');
                    option.value = data.id;
                    option.textContent = data.name;
                    option.selected = true;
                    categorySelect.appendChild(option);
                }

                closeModal();
                showToast('Category created', 'success');
            } catch (err) {
                if (errorEl) {
                    errorEl.textContent = 'Network error while creating category. Please try again.';
                    errorEl.style.display = 'block';
                }
            }
        });
    }
}

/**
 * Toast Notification System
 * Types: 'success', 'error', 'info'
 */
function showToast(message, type = 'info') {
    if (!message) return;

    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let iconSvg = '';
    if (type === 'success') {
        iconSvg = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"></path></svg>';
    } else if (type === 'error') {
        iconSvg = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>';
    } else {
        iconSvg = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
    }

    toast.innerHTML = `${iconSvg}<span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.add('show');
    });

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 200);
    }, 3000);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Automatically fire toasts from URL query parameters and alerts
 */
function initToasts() {
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.get('taskCreated') === 'true' || urlParams.get('created') === 'true') {
        showToast('Task created successfully!', 'success');
    } else if (urlParams.get('deleted') === 'true' || urlParams.get('taskDeleted') === 'true') {
        showToast('Task deleted successfully.', 'info');
    } else if (urlParams.get('statusUpdated') === 'true') {
        showToast('Task status updated.', 'info');
    } else if (urlParams.get('strategyUpdated') === 'true') {
        showToast('Priority strategy updated and tasks recalculated!', 'success');
    } else if (urlParams.get('recalculated') === 'true') {
        showToast('All task scores recalculated successfully.', 'success');
    } else if (urlParams.get('categoryCreated') === 'true') {
        showToast('Category created', 'success');
    } else if (urlParams.get('registered') === 'true') {
        showToast('Account created successfully! Please sign in.', 'success');
    } else if (urlParams.get('loggedOut') === 'true') {
        showToast('You have been signed out safely.', 'info');
    } else if (urlParams.get('userDeactivated') === 'true') {
        showToast('User deactivated.', 'info');
    } else if (urlParams.get('userReactivated') === 'true') {
        showToast('User reactivated.', 'success');
    }

    const serverError = document.querySelector('.alert.alert-danger');
    if (serverError && serverError.textContent.trim()) {
        showToast(serverError.textContent.trim(), 'error');
    }
}

/**
 * Metric Cards Count-Up Animation (800ms)
 */
function initCountUp() {
    const statElements = document.querySelectorAll('.stat-value[data-target]');
    if (!statElements || statElements.length === 0) return;

    statElements.forEach(elem => {
        const target = parseFloat(elem.getAttribute('data-target'));
        if (isNaN(target)) return;

        const isDecimal = String(elem.getAttribute('data-target')).includes('.');
        const duration = 800;
        const startTime = performance.now();

        function step(now) {
            const progress = Math.min((now - startTime) / duration, 1);
            const easeOut = 1 - Math.pow(1 - progress, 3);
            const current = easeOut * target;

            if (isDecimal) {
                elem.textContent = current.toFixed(1);
            } else {
                elem.textContent = Math.round(current);
            }

            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                elem.textContent = isDecimal ? target.toFixed(1) : Math.round(target);
            }
        }

        requestAnimationFrame(step);
    });
}

/**
 * Task Creation Form Handling & Deadline Composition
 */
function initTaskCreateForm() {
    const taskForm = document.getElementById('task-create-form') || document.querySelector('form[action*="/tasks"]');
    if (!taskForm) return;

    taskForm.addEventListener('submit', (e) => {
        const deadlineDateInput = document.getElementById('deadlineDate');
        if (!deadlineDateInput) return;

        const deadlineDate = deadlineDateInput.value;
        const deadlineTimeInput = document.getElementById('deadlineTime');
        const deadlineTime = (deadlineTimeInput && deadlineTimeInput.value) ? deadlineTimeInput.value : '23:59';

        if (!deadlineDate) {
            e.preventDefault();
            e.stopImmediatePropagation();
            showToast('Please select a deadline date', 'error');
            deadlineDateInput.classList.add('input-error');
            deadlineDateInput.focus();
            return;
        }

        const selectedDate = new Date(deadlineDate);
        if (selectedDate < new Date().setHours(0, 0, 0, 0)) {
            e.preventDefault();
            e.stopImmediatePropagation();
            showToast('Deadline cannot be in the past', 'error');
            deadlineDateInput.classList.add('input-error');
            deadlineDateInput.focus();
            return;
        }

        const deadline = deadlineDate ? `${deadlineDate}T${deadlineTime}:00` : null;

        let hiddenDeadline = taskForm.querySelector('input[name="deadline"]');
        if (!hiddenDeadline) {
            hiddenDeadline = document.createElement('input');
            hiddenDeadline.type = 'hidden';
            hiddenDeadline.name = 'deadline';
            taskForm.appendChild(hiddenDeadline);
        }
        hiddenDeadline.value = deadline || '';
    });
}

/**
 * Inline Form Validation for Required Inputs
 */
function initFormValidation() {
    const forms = document.querySelectorAll('form:not(#categoryModalForm)');

    forms.forEach(form => {
        const requiredInputs = form.querySelectorAll('input[required], select[required], textarea[required]');

        requiredInputs.forEach(input => {
            input.addEventListener('input', () => {
                if (input.value && input.value.trim() !== '') {
                    input.classList.remove('input-error');
                    const err = input.parentElement.querySelector('.error-text');
                    if (err) err.remove();
                }
            });
        });

        form.addEventListener('submit', (e) => {
            let hasError = false;
            let firstInvalid = null;

            requiredInputs.forEach(input => {
                if (!input.value || input.value.trim() === '') {
                    hasError = true;
                    input.classList.add('input-error');

                    let err = input.parentElement.querySelector('.error-text');
                    if (!err) {
                        err = document.createElement('span');
                        err.className = 'error-text';
                        err.textContent = 'This field is required.';
                        input.parentElement.appendChild(err);
                    }

                    if (!firstInvalid) {
                        firstInvalid = input;
                    }
                }
            });

            if (hasError) {
                e.preventDefault();
                if (firstInvalid) {
                    firstInvalid.focus();
                }
                showToast('Please fill in all required fields.', 'error');
            }
        });
    });
}

/**
 * Focus Create Task Input from Empty State Button
 */
function initEmptyStateActions() {
    const createBtn = document.getElementById('btn-focus-create');
    if (createBtn) {
        createBtn.addEventListener('click', () => {
            const titleInput = document.getElementById('title');
            if (titleInput) {
                titleInput.focus();
                titleInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }
}

/**
 * Live Stopwatch / Timer Counter
 */
function initTimer() {
    const timerDisplay = document.getElementById('active-timer-display');
    const timerStartTimeInput = document.getElementById('active-timer-start');

    if (!timerDisplay || !timerStartTimeInput || !timerStartTimeInput.value) {
        return;
    }

    const startTimestamp = new Date(timerStartTimeInput.value).getTime();

    function updateCounter() {
        const now = new Date().getTime();
        const diffSeconds = Math.max(0, Math.floor((now - startTimestamp) / 1000));

        const hours = Math.floor(diffSeconds / 3600);
        const minutes = Math.floor((diffSeconds % 3600) / 60);
        const seconds = diffSeconds % 60;

        const formatted = [
            String(hours).padStart(2, '0'),
            String(minutes).padStart(2, '0'),
            String(seconds).padStart(2, '0')
        ].join(':');

        timerDisplay.textContent = formatted;
    }

    updateCounter();
    setInterval(updateCounter, 1000);
}

/**
 * Client-side Dynamic Task Filter
 */
function initFilters() {
    const tierFilter = document.getElementById('filter-tier');
    const statusFilter = document.getElementById('filter-status');
    const searchInput = document.getElementById('filter-search');
    const taskItems = document.querySelectorAll('.task-item');

    if (!tierFilter && !statusFilter && !searchInput) {
        return;
    }

    function applyFilters() {
        const selectedTier = tierFilter ? tierFilter.value.toLowerCase() : '';
        const selectedStatus = statusFilter ? statusFilter.value.toUpperCase() : '';
        const query = searchInput ? searchInput.value.toLowerCase().trim() : '';

        taskItems.forEach(item => {
            const itemTier = (item.dataset.tier || '').toLowerCase();
            const itemStatus = (item.dataset.status || '').toUpperCase();
            const itemTitle = (item.dataset.title || '').toLowerCase();

            const matchesTier = !selectedTier || itemTier === selectedTier;
            const matchesStatus = !selectedStatus || itemStatus === selectedStatus;
            const matchesQuery = !query || itemTitle.includes(query);

            if (matchesTier && matchesStatus && matchesQuery) {
                item.style.display = 'flex';
            } else {
                item.style.display = 'none';
            }
        });
    }

    if (tierFilter) tierFilter.addEventListener('change', applyFilters);
    if (statusFilter) statusFilter.addEventListener('change', applyFilters);
    if (searchInput) searchInput.addEventListener('input', applyFilters);
}

/**
 * Confirmation Dialogs for Destructive Actions
 */
function initConfirmations() {
    document.querySelectorAll('[data-confirm]').forEach(element => {
        element.addEventListener('click', (event) => {
            const message = element.getAttribute('data-confirm') || 'Are you sure?';
            if (!confirm(message)) {
                event.preventDefault();
            }
        });
    });
}

/**
 * Quickly Toggle Task Status via POST /tasks
 */
function updateTaskStatus(taskId, newStatus, contextPath) {
    if (!taskId || !newStatus) return;

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = (contextPath || '') + '/tasks';

    const methodField = document.createElement('input');
    methodField.type = 'hidden';
    methodField.name = '_method';
    methodField.value = 'PUT';
    form.appendChild(methodField);

    const idField = document.createElement('input');
    idField.type = 'hidden';
    idField.name = 'id';
    idField.value = taskId;
    form.appendChild(idField);

    const statusField = document.createElement('input');
    statusField.type = 'hidden';
    statusField.name = 'status';
    statusField.value = newStatus;
    form.appendChild(statusField);

    document.body.appendChild(form);
    form.submit();
}

/**
 * Mobile Navigation Menu Toggle (Responsive drawer on <= 768px screens)
 */
function initMobileNav() {
    const hamburger = document.getElementById('nav-toggle') || document.querySelector('.nav-hamburger');
    const navLinks = document.getElementById('primary-nav') || document.querySelector('.nav-links');
    if (!hamburger || !navLinks) return;

    hamburger.addEventListener('click', (e) => {
        e.stopPropagation();
        navLinks.classList.toggle('nav-open');
        const isOpen = navLinks.classList.contains('nav-open');
        hamburger.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
    });

    // Close when clicking anywhere outside the menu
    document.addEventListener('click', (e) => {
        if (!hamburger.contains(e.target) && !navLinks.contains(e.target) && navLinks.classList.contains('nav-open')) {
            navLinks.classList.remove('nav-open');
            hamburger.setAttribute('aria-expanded', 'false');
        }
    });
}
