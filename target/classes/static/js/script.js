// Конфигурация
const API_BASE_URL = 'http://localhost:8080/api';
const DEPARTMENT_NAMES = {
    '1': 'БАППиБТ',
    '2': 'ВССиИБ',
    '3': 'ЖДСТУ',
    '4': 'КЭТиТ',
    '5': 'ЛиУТС',
    '6': 'ЛТСТ',
    '7': 'УТБиИС',
    '8': 'УЭРиБТ',
    '9': 'ХиИЭ',
    '10': 'ЦТУТП',
    '11': 'Адм',
    '12': 'Уч. отд',
    '13': 'Отд. инф'
};

// Состояние приложения
let contacts = [];
let currentUser = null;
let currentTab = 'all';

// DOM элементы
const elements = {
    contactsContainer: document.getElementById('contacts-container'),
    noResults: document.getElementById('no-results'),
    searchInput: document.getElementById('search-input'),
    addContactBtn: document.getElementById('add-contact-btn'),
    authButtons: document.getElementById('auth-buttons'),
    userMenu: document.getElementById('user-menu'),
    usernameDisplay: document.getElementById('username-display'),
    userMenuButton: document.getElementById('user-menu-button'),
    userDropdown: document.getElementById('user-dropdown'),
    logoutBtn: document.getElementById('logout-btn'),
    loginBtn: document.getElementById('login-btn'),
    departmentList: document.getElementById('department-list'),
    authModal: document.getElementById('auth-modal'),
    loginForm: document.getElementById('login-form'),
    loginEmail: document.getElementById('login-email'),
    loginPassword: document.getElementById('login-password'),
    closeAuthModalBtn: document.getElementById('close-auth-modal'),
    editModal: document.getElementById('edit-modal'),
    modalTitle: document.getElementById('modal-title'),
    contactForm: document.getElementById('contact-form'),
    contactIdInput: document.getElementById('contact-id'),
    editName: document.getElementById('edit-name'),
    editDepartment: document.getElementById('edit-department'),
    editPersonalPhone: document.getElementById('edit-personal-phone'),
    editWorkPhone: document.getElementById('edit-work-phone'),
    editEmail: document.getElementById('edit-email'),
    editRoom: document.getElementById('edit-room'),
    editAdditionalInfo: document.getElementById('edit-additional-info'),
    editStatusNote: document.getElementById('edit-status-note'),
    deleteBtn: document.getElementById('delete-btn'),
    closeEditModalBtn: document.getElementById('close-edit-modal'),
    cancelEditBtn: document.getElementById('cancel-edit-btn')
};

// API функции
async function fetchContacts() {
    try {
        const response = await fetch(`${API_BASE_URL}/employees`);
        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }
        const data = await response.json();
        console.log('Получены контакты:', data);
        return data;
    } catch (error) {
        console.error('Ошибка при получении контактов:', error);
        showToast('Ошибка загрузки данных', 'error');
        return [];
    }
}

async function saveContactToAPI(contact) {
    try {
        const method = contact.id ? 'PUT' : 'POST';
        const url = contact.id ? `${API_BASE_URL}/employees/${contact.id}` : `${API_BASE_URL}/employees`;

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(contact)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Ошибка сохранения');
        }

        return await response.json();
    } catch (error) {
        console.error('Ошибка сохранения контакта:', error);
        showToast(error.message || 'Ошибка при сохранении', 'error');
        return null;
    }
}

async function deleteContactFromAPI(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/employees/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        return true;
    } catch (error) {
        console.error('Ошибка удаления контакта:', error);
        showToast('Ошибка при удалении', 'error');
        return false;
    }
}

// Вспомогательные функции
function formatPhoneNumber(phone) {
    if (!phone) return '';
    // Удаляем все нецифровые символы
    const cleaned = phone.replace(/\D/g, '');

    // Форматируем в зависимости от длины номера
    if (cleaned.length === 11) {
        // Российский номер: +7 (XXX) XXX-XX-XX
        return `+${cleaned[0]} (${cleaned.substring(1, 4)}) ${cleaned.substring(4, 7)}-${cleaned.substring(7, 9)}-${cleaned.substring(9)}`;
    } else if (cleaned.length === 12) {
        // Международный номер с кодом страны из 2 цифр: +XX (XXX) XXX-XXXX
        return `+${cleaned.substring(0, 2)} (${cleaned.substring(2, 5)}) ${cleaned.substring(5, 8)}-${cleaned.substring(8)}`;
    } else if (cleaned.length === 10) {
        // Номер без кода страны: (XXX) XXX-XXXX
        return `(${cleaned.substring(0, 3)}) ${cleaned.substring(3, 6)}-${cleaned.substring(6)}`;
    }

    // Если номер не подходит под стандартные форматы, возвращаем как есть
    return phone;
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `fixed bottom-4 right-4 px-4 py-2 rounded shadow-lg ${
        type === 'error' ? 'bg-red-500' : 'bg-green-500'
    } text-white`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 5000);
}

function debounce(func, timeout = 300) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
}

// Основные функции
async function renderContacts() {
    try {
        const searchTerm = elements.searchInput.value.toLowerCase();
        let filteredContacts = await fetchContacts();

        // Фильтрация по текущей вкладке
        if (currentTab !== 'all') {
            filteredContacts = filteredContacts.filter(contact => contact.department === currentTab);
        }

        // Фильтрация по поисковому запросу
        if (searchTerm) {
            filteredContacts = filteredContacts.filter(contact => {
                const fullName = `${contact.lastName} ${contact.firstName} ${contact.middleName || ''}`.toLowerCase();
                const departmentName = DEPARTMENT_NAMES[contact.department] || '';
                return (
                    fullName.includes(searchTerm) ||
                    departmentName.toLowerCase().includes(searchTerm) ||
                    (contact.workPhone && contact.workPhone.replace(/[^\d]/g, '').includes(searchTerm.replace(/[^\d]/g, ''))) ||
                    (contact.email && contact.email.toLowerCase().includes(searchTerm)) ||
                    (contact.room && contact.room.toLowerCase().includes(searchTerm))
                );
            });
        }

        // Сортировка по фамилии и имени
        filteredContacts.sort((a, b) => {
            const nameA = `${a.lastName} ${a.firstName}`;
            const nameB = `${b.lastName} ${b.firstName}`;
            return nameA.localeCompare(nameB);
        });

        renderContactCards(filteredContacts);
    } catch (error) {
        console.error('Ошибка рендеринга контактов:', error);
        showToast('Ошибка отображения данных', 'error');
    }
}

function renderContactCards(contacts) {
    elements.contactsContainer.innerHTML = '';

    if (!contacts || contacts.length === 0) {
        elements.noResults.classList.remove('hidden');
        return;
    }

    elements.noResults.classList.add('hidden');

    contacts.forEach(contact => {
        const card = document.createElement('div');
        card.className = 'contact-card bg-white rounded-lg shadow p-6 flex flex-col mb-4';

        const fullName = `${contact.lastName} ${contact.firstName} ${contact.middleName || ''}`.trim();
        const departmentId = contact.department || contact.departmentId || contact.departmentID;
        const departmentName = DEPARTMENT_NAMES[departmentId];

        let editDeleteButtons = '';
        if (currentUser && (currentUser.role === 'ADMIN' ||
            (currentUser.role === 'MODERATOR' && currentUser.department === contact.department))) {
            editDeleteButtons = `
                <div class="flex justify-end mt-4 space-x-2">
                    <button onclick="openEditContactModal(${contact.id})"
                            class="text-blue-600 hover:text-blue-800 px-2 py-1 rounded hover:bg-blue-50">
                        <i class="fas fa-edit"></i> Редактировать
                    </button>
                    <button onclick="confirmDeleteContact(${contact.id})"
                            class="text-red-600 hover:text-red-800 px-2 py-1 rounded hover:bg-red-50">
                        <i class="fas fa-trash-alt"></i> Удалить
                    </button>
                </div>
            `;
        }

        card.innerHTML = `
            <div class="flex justify-between items-start mb-4">
                <h3 class="text-xl font-bold">${fullName}</h3>
                <span class="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded">
                    ${departmentName}
                </span>
            </div>
            <div class="mt-auto space-y-3">
                ${renderContactDetail('Рабочий телефон', contact.workPhone, true)}
                ${contact.personalPhone ? renderContactDetail('Личный телефон', contact.personalPhone, true) : ''}
                ${renderContactDetail('Email', contact.email, false, false, true)}
                ${contact.additionalInfo ? renderContactDetail('Telegram', contact.additionalInfo, false, false, false, true) : ''}
                ${contact.officeNumber ? renderContactDetail('Кабинет', contact.officeNumber) : ''}
                ${contact.statusNote ? renderContactDetail('Заметки', contact.statusNote) : ''}
            </div>
            ${editDeleteButtons}
        `;

        elements.contactsContainer.appendChild(card);
    });
}

function renderContactDetail(label, value, isPhone = false, ext = null, isEmail = false, isTelegram = false) {
    if (!value) return '';

    let content = value;
    if (isPhone) {
        content = `<a href="tel:${value}" class="text-blue-600 hover:underline">${formatPhoneNumber(value)}</a>`;
        if (ext) {
            content += ` доб. ${ext}`;
        }
    } else if (isEmail) {
        content = `<a href="mailto:${value}" class="text-blue-600 hover:underline">${value}</a>`;
    } else if (isTelegram) {
        const link = value.startsWith('@') ? `https://t.me/${value.substring(1)}` : value;
        content = `<a href="${link}" target="_blank" class="text-blue-600 hover:underline">${value}</a>`;
    }

    return `
        <div>
            <div class="text-sm text-gray-500 mb-1">${label}</div>
            <div class="text-gray-800">${content}</div>
        </div>
    `;
}

// Управление модальными окнами
function openEditContactModal(id = null) {
    if (id) {
        const contact = contacts.find(c => c.id === id);
        if (!contact) return;

        elements.contactIdInput.value = contact.id;
        elements.editName.value = `${contact.lastName} ${contact.firstName} ${contact.middleName || ''}`.trim();
        elements.editDepartment.value = contact.department;
        elements.editPersonalPhone.value = contact.personalPhone || '';
        elements.editWorkPhone.value = contact.workPhone || '';
        elements.editEmail.value = contact.email || '';
        elements.editRoom.value = contact.room || '';
        elements.editAdditionalInfo.value = contact.additionalInfo || '';
        elements.editStatusNote.value = contact.statusNote || '';

        elements.modalTitle.textContent = 'Редактировать сотрудника';
        elements.deleteBtn.classList.remove('hidden');
    } else {
        elements.contactForm.reset();
        elements.contactIdInput.value = '';
        elements.modalTitle.textContent = 'Добавить сотрудника';
        elements.deleteBtn.classList.add('hidden');
    }

    // Для модераторов - фиксируем отдел
    if (currentUser && currentUser.role === 'MODERATOR') {
        elements.editDepartment.value = currentUser.department;
        elements.editDepartment.disabled = true;
    } else {
        elements.editDepartment.disabled = false;
    }

    elements.editModal.style.display = 'block';
}

function closeEditModal() {
    elements.editModal.style.display = 'none';
}

// Обработчики CRUD
async function saveContact() {
    const nameParts = elements.editName.value.trim().split(' ');
    const contact = {
        id: elements.contactIdInput.value ? parseInt(elements.contactIdInput.value) : null,
        lastName: nameParts[0] || '',
        firstName: nameParts[1] || '',
        middleName: nameParts.slice(2).join(' ') || null,
        department: elements.editDepartment.value,
        workPhone: elements.editWorkPhone.value.trim(),
        personalPhone: elements.editPersonalPhone.value.trim() || null,
        email: elements.editEmail.value.trim(),
        room: elements.editRoom.value.trim(),
        additionalInfo: elements.editAdditionalInfo.value.trim() || null,
        statusNote: elements.editStatusNote.value.trim() || null
    };

    const savedContact = await saveContactToAPI(contact);
    if (savedContact) {
        showToast('Контакт успешно сохранен');
        closeEditModal();
        await renderContacts();
    }
}

async function deleteContact(id) {
    if (!confirm('Вы уверены, что хотите удалить этого сотрудника?')) return;

    const isDeleted = await deleteContactFromAPI(id);
    if (isDeleted) {
        showToast('Контакт успешно удален');
        closeEditModal();
        await renderContacts();
    }
}

function confirmDeleteContact(id) {
    if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
        deleteContact(id);
    }
}

// Управление вкладками
function openTab(tabName) {
    currentTab = tabName;
    document.querySelectorAll('.department-btn').forEach(btn => {
        const isActive = btn.dataset.department === tabName;
        btn.classList.toggle('bg-blue-100', isActive);
        btn.classList.toggle('text-blue-700', isActive);
    });
    renderContacts();
}

// Авторизация (заглушка)
function handleLogin() {
    const email = elements.loginEmail.value;
    const password = elements.loginPassword.value;

    // Временная заглушка - замените на реальную авторизацию
    if (email === 'admin@company.com' && password === 'admin123') {
        currentUser = {
            email: email,
            role: 'ADMIN',
            name: 'Администратор',
            department: 'all'
        };
        updateUIAfterLogin();
        closeAuthModal();
        showToast('Успешный вход');
    } else if (email === 'moder@company.com' && password === 'moder123') {
        currentUser = {
            email: email,
            role: 'MODERATOR',
            name: 'Модератор',
            department: '1' // Пример: модератор для отдела с ID 1
        };
        updateUIAfterLogin();
        closeAuthModal();
        showToast('Успешный вход');
    } else {
        showToast('Неверные учетные данные', 'error');
    }
}

function updateUIAfterLogin() {
    elements.authButtons.classList.add('hidden');
    elements.userMenu.classList.remove('hidden');
    elements.usernameDisplay.textContent = currentUser.name;
    elements.addContactBtn.classList.remove('hidden');
}

function handleLogout() {
    currentUser = null;
    elements.userMenu.classList.add('hidden');
    elements.authButtons.classList.remove('hidden');
    elements.addContactBtn.classList.add('hidden');
    elements.userDropdown.classList.add('hidden');
    showToast('Вы вышли из системы');
}

function openAuthModal() {
    elements.authModal.style.display = 'block';
}

function closeAuthModal() {
    elements.authModal.style.display = 'none';
    elements.loginForm.reset();
}

// Инициализация
function setupEventListeners() {
    // Поиск
    elements.searchInput.addEventListener('input', debounce(renderContacts, 300));

    // Модальные окна
    elements.loginBtn.addEventListener('click', openAuthModal);
    elements.closeAuthModalBtn.addEventListener('click', closeAuthModal);
    elements.addContactBtn.addEventListener('click', () => openEditContactModal());
    elements.closeEditModalBtn.addEventListener('click', closeEditModal);
    elements.cancelEditBtn.addEventListener('click', closeEditModal);

    // Формы
    elements.loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        handleLogin();
    });
    elements.contactForm.addEventListener('submit', (e) => {
        e.preventDefault();
        saveContact();
    });

    // Кнопки управления
    elements.userMenuButton.addEventListener('click', () => {
        elements.userDropdown.classList.toggle('hidden');
    });
    elements.logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        handleLogout();
    });

    // Вкладки отделов
    elements.departmentList.addEventListener('click', (e) => {
        if (e.target.classList.contains('department-btn')) {
            const department = e.target.dataset.department;
            openTab(department);
        }
    });

    // Закрытие модальных окон по клику вне
    window.addEventListener('click', (event) => {
        if (event.target === elements.authModal) closeAuthModal();
        if (event.target === elements.editModal) closeEditModal();
    });
}

// Запуск приложения
document.addEventListener('DOMContentLoaded', async () => {
    setupEventListeners();
    await renderContacts();
    openTab('all');
});

// Глобальные функции для вызова из HTML
window.openEditContactModal = openEditContactModal;
window.confirmDeleteContact = confirmDeleteContact;