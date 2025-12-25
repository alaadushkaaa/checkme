export function initDragAndDrop() {
    const taskList = document.querySelector('.task-selection-container');
    if (!taskList) {
        return;
    }

    const taskElements = taskList.querySelectorAll('.task-item');

    taskElements.forEach((task) => {
        task.draggable = true;
        
        task.addEventListener('dragstart', (evt) => {
            evt.target.classList.add('dragging');
            evt.dataTransfer.effectAllowed = 'move';
            
            setTimeout(() => {
                evt.target.style.opacity = '0.4';
            }, 0);
        });

        task.addEventListener('dragend', (evt) => {
            evt.target.classList.remove('dragging');
            evt.target.style.opacity = '';
            evt.target.style.transform = '';
        });
    });

    taskList.addEventListener('dragover', (evt) => {
        evt.preventDefault();
        evt.dataTransfer.dropEffect = 'move';

        const draggingElement = taskList.querySelector('.dragging');
        if (!draggingElement) return;

        const afterElement = getDragAfterElement(taskList, evt.clientY);

        if (afterElement == null) {
            taskList.appendChild(draggingElement);
        } else {
            taskList.insertBefore(draggingElement, afterElement);
        }
    });

    // Функция для определения предшествующего элемента для вставки
    function getDragAfterElement(container, y) {
        const draggableElements = [...container.querySelectorAll('.task-item:not(.dragging)')];

        return draggableElements.reduce((closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = y - box.top - box.height / 2;

            if (offset < 0 && offset > closest.offset) {
                return { offset: offset, element: child };
            } else {
                return closest;
            }
        }, { offset: Number.NEGATIVE_INFINITY }).element;
    }
}
window.initDragAndDrop = initDragAndDrop;

export function getTasksInOrder() {
    const taskList = document.querySelector('.task-selection-container');
    const taskElements = taskList.querySelectorAll('.task-item');

    const order = [];
    taskElements.forEach((item, index) => {
        const taskId = item.getAttribute('id');
        if (taskId) {
            order.push(taskId);
        }
    });
    return order;
}

window.getTasksInOrder = getTasksInOrder;

export default {
    initDragAndDrop: initDragAndDrop,
    getTasksInOrder: getTasksInOrder
};