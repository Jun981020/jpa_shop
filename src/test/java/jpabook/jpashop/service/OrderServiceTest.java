package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughQuantityException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception{
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER,getOrder.getStatus(),"상품의 상태는 ORDER 여야 합니다");
        assertEquals(1,getOrder.getOrderItems().size(),"주문한 상품의 종류수가 정확해야 한다.");
        assertEquals(10000*2,getOrder.getTotalPrice(),"주문 가격은 가격 * 수량이다.");
        assertEquals(8,book.getStockQuantity(),"주문한 수량만큼 재고가 줄어야한다.");

    }

    @Test
    public void 주문취소() throws Exception{
        //given
        Member member = createMember();
        Item book = createBook("시골 jpa",10000,20);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL,getOrder.getStatus(),"주문 상태가 캔슬로 바껴야한다.");
        assertEquals(20,book.getStockQuantity(),"재고 수량이 원복되어야 한다.");

    }

    @Test
    public void 상품주문_재고수량초과() throws Exception{
        //given
        Member member = createMember();
        Item book = createBook("시골 JPA", 10000, 10);
        //when
        int orderCount = 11;
        //then
        assertThrows(NotEnoughQuantityException.class,
                () -> orderService.order(member.getId(),book.getId(),orderCount)
                );
    }


    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울","동대문구","정릉천동로"));
        em.persist(member);
        return member;
    }
}